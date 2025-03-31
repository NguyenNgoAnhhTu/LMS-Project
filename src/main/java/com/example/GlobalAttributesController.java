package com.example;

import com.example.module_group.ModuleGroup;
import com.example.module_group.ModuleGroupService;
import com.example.role.Role;
import com.example.role.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalAttributesController {

    @Autowired
    private ModuleGroupService moduleGroupService;
    @Autowired
    private RoleService roleService;// Service hoặc repository để lấy dữ liệu

    @ModelAttribute("moduleGroups")
    public List<ModuleGroup> getModuleGroups() {
        return moduleGroupService.getAllModuleGroups(); // Trả về danh sách `ModuleGroup`
    }

    @ModelAttribute("roles")
    public List<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        //String roles = authentication.getAuthorities().toString();


        String roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        List<Role> list = roleService.getAllRoles().stream().filter(role -> !roles.equals(role.getName())).toList(); // Trả về danh sách `Role`
        return list;
    }


    @ModelAttribute("curr_role")
    public String currRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        //String roles = authentication.getAuthorities().toString();


        String roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        return roles;
    }

}
