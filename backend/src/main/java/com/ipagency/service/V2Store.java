package com.ipagency.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ipagency.common.*;
import jakarta.validation.Validator;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/** Shared typed persistence helpers; query columns are supplied only by backend code. */
@Component
public class V2Store {
    private final ApplicationContext context;
    private final Validator validator;
    public V2Store(ApplicationContext context, Validator validator) {
        this.context = context;
        this.validator = validator;
    }
    @SuppressWarnings("unchecked")
    public <T> BaseMapper<T> mapper(Class<T> type) {
        String name = type.getSimpleName();
        return (BaseMapper<T>) context.getBean(Character.toLowerCase(name.charAt(0)) + name.substring(1) + "Mapper");
    }
    public <T> T get(Class<T> type, Long id) {
        if (id == null) throw new BusinessException("缺少对象 ID");
        T value = mapper(type).selectById(id);
        if (value == null) throw new BusinessException("数据不存在", HttpStatus.NOT_FOUND);
        return value;
    }
    public <T> T lock(Class<T> type, Long id) {
        T value = mapper(type).selectOne(new QueryWrapper<T>().eq("id", id).last("FOR UPDATE"));
        if (value == null) throw new BusinessException("数据不存在", HttpStatus.NOT_FOUND);
        return value;
    }
    public <T> T one(Class<T> type, QueryWrapper<T> query) { return mapper(type).selectOne(query); }
    public <T> long count(Class<T> type, QueryWrapper<T> query) { return mapper(type).selectCount(query); }
    public <T> PageResult<T> page(Class<T> type, QueryWrapper<T> query, long number, long size) {
        if (number < 1 || size < 1 || size > 100) throw new BusinessException("分页参数须为正数，pageSize 最大为 100");
        return PageResult.from(mapper(type).selectPage(new Page<>(number, size), query));
    }
    public void validate(Object value) {
        var errors = validator.validate(value);
        if (!errors.isEmpty()) {
            var error = errors.iterator().next();
            throw new BusinessException(error.getPropertyPath() + ": " + error.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T insert(T value) {
        validate(value);
        var mapper = mapper((Class<T>) value.getClass());
        mapper.insert(value);
        refresh(value, mapper);
        return value;
    }
    @SuppressWarnings("unchecked")
    public <T> T update(T value) {
        validate(value);
        var mapper = mapper((Class<T>) value.getClass());
        mapper.updateById(value);
        refresh(value, mapper);
        return value;
    }
    private <T> void refresh(T value, BaseMapper<T> mapper) {
        Long id = (Long) new org.springframework.beans.BeanWrapperImpl(value).getPropertyValue("id");
        T persisted = mapper.selectById(id);
        // Return database defaults and generated timestamps as well as the caller's supplied fields.
        if (persisted != null) org.springframework.beans.BeanUtils.copyProperties(persisted, value);
    }
}
