package com.example.springai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springai.entity.PromptTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 提示词模板 Mapper
 *
 * @Author SuTao
 * @Date 2026/3/17
 */
@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplateEntity> {

    /**
     * 增加使用次数
     */
    @Update("UPDATE prompt_template SET use_count = use_count + 1, updated_at = NOW() WHERE id = #{id}")
    int incrementUseCount(@Param("id") String id);

    /**
     * 清除所有默认标记
     */
    @Update("UPDATE prompt_template SET is_default = false WHERE is_default = true")
    int clearAllDefault();
}
