package com.aquarius.wizard.player.server.online.infrastructure.persistence.mapper;

import com.aquarius.wizard.player.server.online.infrastructure.persistence.entity.OnlineTrackEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the online track index table.
 */
@Mapper
public interface OnlineTrackMapper extends BaseMapper<OnlineTrackEntity> {
}
