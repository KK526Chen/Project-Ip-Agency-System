package com.ipagency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ipagency.entity.CasePriority;

public interface CasePriorityMapper extends BaseMapper<CasePriority> {
    @org.apache.ibatis.annotations.Select("SELECT id, case_id, country, priority_no, priority_date, create_time, update_time, is_deleted FROM case_priority WHERE case_id = #{caseId} AND priority_no = #{number}")
    CasePriority findIncludingDeleted(@org.apache.ibatis.annotations.Param("caseId") Long caseId,
                                      @org.apache.ibatis.annotations.Param("number") String number);

    @org.apache.ibatis.annotations.Update("UPDATE case_priority SET country=#{country}, priority_date=#{priorityDate}, is_deleted=0 WHERE id=#{id} AND case_id=#{caseId}")
    int restore(CasePriority priority);
}
