package com.shiyi.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.shiyi.gulimall.product.service.CategoryBrandRelationService;
import com.shiyi.gulimall.product.vo.Catelog2Vo;
import jdk.nashorn.internal.ir.CallNode;
import jodd.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ThreadUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.product.dao.CategoryDao;
import com.shiyi.gulimall.product.entity.CategoryEntity;
import com.shiyi.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树型结构
        //2.1)、找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntitie -> {
            return categoryEntitie.getParentCid() == 0;
        }).map(menu->{
            menu.setChildren(getChildren(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort())- (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    //2,25,225
    @Override
    public Long[] findCatelogPath(Long catelogId) {

        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[0]);
    }

    @Caching(evict = {
            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
            @CacheEvict(value = "category",key = "#root.methodName"),
    })

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Cacheable(value = "category",key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }


    @Cacheable(value = "category",key = "#root.methodName",sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson(){


        log.error("{}，查询了数据库",Thread.currentThread().getId());

        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        //1、先查出所有一级分类
        List<CategoryEntity> level1Entities = getParentCid(categoryEntities,0L);

        //2、封装数据
        //2.1、转成以一级分类id为key，Catelog2Vo为value的Map
        Map<String, List<Catelog2Vo>> collect = level1Entities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
            //查出某个一级分类的二级分类
            List<CategoryEntity> level2Entities = getParentCid(categoryEntities,l1.getCatId());
            //封装成二级分类的Vo数据
            List<Catelog2Vo> catelog2VoList = level2Entities.stream().map(l2 -> {
                //查出某个二级分类的三级分类
                List<CategoryEntity> level3Entities = getParentCid(categoryEntities,l2.getCatId());
                //封装成三级分类的Vo数据
                List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Entities.stream().map(l3 -> {
                    Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                    return catelog3Vo;
                }).collect(Collectors.toList());

                Catelog2Vo catelog2Vo = new Catelog2Vo(l1.getCatId().toString(), catelog3VoList, l2.getCatId().toString(), l2.getName());
                return catelog2Vo;
            }).collect(Collectors.toList());
            return catelog2VoList;
        }));

        return collect;
    }


    /**
     * 使用Redis分布式锁
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson1(){
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        //缓存有
        if(!StringUtils.isEmpty(catalogJSON)){
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        }
        //缓存没有
        return getCatelogJsonFromDBByRedisLock();

    }

    /**
     * 使用Redisson分布式锁
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson2(){
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        //缓存有
        if(!StringUtils.isEmpty(catalogJSON)){
            log.error("{},第一次获取缓存成功，直接返回，没有查询数据库",Thread.currentThread().getId());
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        }
        //缓存没有
        return getCatelogJsonFromDBByRedissonLock();

    }

    /**
     * 使用本地锁synchronized
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson3(){
//        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
//        //缓存有
//        if(!StringUtils.isEmpty(catalogJSON)){
//            log.error("{},第一次获取缓存成功，直接返回，没有查询数据库",Thread.currentThread().getId());
//            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
//        }
        return getCatelogJsonFromDBByLocalLock();
    }


    /**
     * 使用本地锁synchronized/JUC(Lock)，优点性能较高，缺点由于项目上线是分布式的，有多个相同模块，而不同机器中的容器是不一样，
     * 例如有8个gulimall——product，则会使用8个本地锁，查8次数据库
     * Tips：将加入缓存的操作放到锁内，不然可能会发生在加入缓存的同时，有其他线程判断缓存为空，进而再查数据库
     * @return
     */
    //从数据库查询并封装数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBByLocalLock() {

        //Springboot容器中对象都是单例的，故this都是同一个categoryService
        synchronized (this){

            //抢到锁后，先判断上一个抢到锁的进程是否将结果加入缓存中
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            //缓存有
            if(!StringUtils.isEmpty(catalogJSON)){
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            }
            //缓存没有
            List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

            log.error("{},查询了数据库",Thread.currentThread().getId());
            //1、先查出所有一级分类
            List<CategoryEntity> level1Entities = getParentCid(categoryEntities,0L);

            //2、封装数据
            //2.1、转成以一级分类id为key，Catelog2Vo为value的Map
            Map<String, List<Catelog2Vo>> collect = level1Entities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                //查出某个一级分类的二级分类
                List<CategoryEntity> level2Entities = getParentCid(categoryEntities,l1.getCatId());
                //封装成二级分类的Vo数据
                List<Catelog2Vo> catelog2VoList = level2Entities.stream().map(l2 -> {
                    //查出某个二级分类的三级分类
                    List<CategoryEntity> level3Entities = getParentCid(categoryEntities,l2.getCatId());
                    //封装成三级分类的Vo数据
                    List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Entities.stream().map(l3 -> {
                        Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        return catelog3Vo;
                    }).collect(Collectors.toList());

                    Catelog2Vo catelog2Vo = new Catelog2Vo(l1.getCatId().toString(), catelog3VoList, l2.getCatId().toString(), l2.getName());
                    return catelog2Vo;
                }).collect(Collectors.toList());
                return catelog2VoList;
            }));

            //将结果放入缓存
            String s = JSON.toJSONString(collect);
            redisTemplate.opsForValue().set("catalogJSON",s);

            return collect;
        }
    }


    /**
     * 使用Redis作为分布式锁（占坑）
     * 容易忽略的问题：
     * 1、设置的value不能是固定的字符，需要较大的，不固定的，例如UUID，避免出现删除别人的锁
     * 需要同时设置过期时间以及判断该key是否存在，即set EX NX，原子操作，否则会出现过期时间没设置到，发生死锁
     * 2、完成业务后，需要解锁，解锁前需要判断该锁是不是自己的，即判断value值是不是之前设置时的UUID，只有自己的才能解锁，
     * 而为了保证这个取值判断和删除的操作是原子操作，需要使用lua脚本，避免在刚好取完值后key过期，其他进程设置了其他的锁（即key相同，value不同），释放了别人的锁的情况。
     * @return
     */
    //从数据库查询并封装数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBByRedisLock() {

        UUID uuid = UUID.randomUUID();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid.toString(), 300, TimeUnit.SECONDS);

        if(lock){

            Map<String, List<Catelog2Vo>> collect = null;
            try{
                //TODO 获取到锁之后先查缓存，缓存有直接返回
                String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
                //缓存有
                if(!StringUtils.isEmpty(catalogJSON)){
                    return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
                }

                List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

                //1、先查出所有一级分类
                List<CategoryEntity> level1Entities = getParentCid(categoryEntities,0L);

                //2、封装数据
                //2.1、转成以一级分类id为key，Catelog2Vo为value的Map
                collect = level1Entities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                    //查出某个一级分类的二级分类
                    List<CategoryEntity> level2Entities = getParentCid(categoryEntities,l1.getCatId());
                    //封装成二级分类的Vo数据
                    List<Catelog2Vo> catelog2VoList = level2Entities.stream().map(l2 -> {
                        //查出某个二级分类的三级分类
                        List<CategoryEntity> level3Entities = getParentCid(categoryEntities,l2.getCatId());
                        //封装成三级分类的Vo数据
                        List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Entities.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        Catelog2Vo catelog2Vo = new Catelog2Vo(l1.getCatId().toString(), catelog3VoList, l2.getCatId().toString(), l2.getName());
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                    return catelog2VoList;
                }));

                //将结果放入缓存
                String s = JSON.toJSONString(collect);
                redisTemplate.opsForValue().set("catalogJSON",s);
            }finally {
                //释放锁
                // lua 脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 删除锁
                redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList("lock"), uuid);
            }

            return collect;
        }else{
            //采用自旋来获取锁
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDBByRedisLock();
        }
    }

    /**
     * 使用Redisson提供的API锁
     * @return
     */
    //从数据库查询并封装数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBByRedissonLock() {

        RLock lock = redissonClient.getLock("CatelogJson-lock");
        //阻塞式获取锁
        lock.lock();
        Map<String, List<Catelog2Vo>> collect = null;
        try{
            //获取锁成功后再去判断缓存有无
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            //缓存有
            if(!StringUtils.isEmpty(catalogJSON)){
                log.error("{}，获取锁后获取缓存成功，没有查询数据库",Thread.currentThread().getId());
                return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            }

            List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

            //1、先查出所有一级分类
            List<CategoryEntity> level1Entities = getParentCid(categoryEntities,0L);

            //2、封装数据
            //2.1、转成以一级分类id为key，Catelog2Vo为value的Map
            collect = level1Entities.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), l1 -> {
                //查出某个一级分类的二级分类
                List<CategoryEntity> level2Entities = getParentCid(categoryEntities,l1.getCatId());
                //封装成二级分类的Vo数据
                List<Catelog2Vo> catelog2VoList = level2Entities.stream().map(l2 -> {
                    //查出某个二级分类的三级分类
                    List<CategoryEntity> level3Entities = getParentCid(categoryEntities,l2.getCatId());
                    //封装成三级分类的Vo数据
                    List<Catelog2Vo.Catelog3Vo> catelog3VoList = level3Entities.stream().map(l3 -> {
                        Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                        return catelog3Vo;
                    }).collect(Collectors.toList());

                    Catelog2Vo catelog2Vo = new Catelog2Vo(l1.getCatId().toString(), catelog3VoList, l2.getCatId().toString(), l2.getName());
                    return catelog2Vo;
                }).collect(Collectors.toList());
                return catelog2VoList;
            }));
            log.error("{}，缓存中没有，直接查询数据库",Thread.currentThread().getId());
            //将结果放入缓存
            String s = JSON.toJSONString(collect);
            redisTemplate.opsForValue().set("catalogJSON",s);
        }finally {
          lock.unlock();
        }

        return collect;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> list,Long parentCatId) {
        List<CategoryEntity> collect = list.stream().filter(item -> item.getParentCid() == parentCatId).collect(Collectors.toList());
        return collect;
    }

    //225,25,2
    public List<Long> findParentPath(Long catelogId,List<Long> paths){
        //1.收集当前结点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }




    public List<CategoryEntity> getChildren(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //递归找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{       //菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort())- (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}