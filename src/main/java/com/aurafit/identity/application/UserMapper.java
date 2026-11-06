package com.aurafit.identity.application;

import com.aurafit.identity.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserView toView(User user);
}
