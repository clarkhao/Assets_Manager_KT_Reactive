package com.clark.totoro.assets.service

import com.clark.totoro.assets.config.RbacConfig
import com.clark.totoro.assets.model.DoubleRole
import kotlinx.coroutines.flow.asFlow
import org.springframework.stereotype.Service

@Service
class RbacService(val rbac: RbacConfig) {
    fun preSetRoles(): List<String> {
        //role::guest, role:limited, role::user, role::admin, role::root
        //add roles
        val roleAdding = listOf<DoubleRole>(
            DoubleRole("guest", "user"),
            DoubleRole("user", "admin"),
            DoubleRole("clarkhao", "root"),
            DoubleRole("YWM4OTBlNTQtMjk0MS00ZmY2LWIxZTgtMWExNDgzNTMyMmVh", "root")
        )
        roleAdding.map { rbac.cas().addRoleForUser(it.memberRole, it.fatherRole) }
        return rbac.cas().allRoles
    }
    fun preSetPolicies(): List<List<String>> {
        //add policies
        val policyAdding = arrayOf<Array<String>>(
            arrayOf("guest", "portfolio_file", "read"),
            arrayOf("user", "portfolio_file", "write"),
            arrayOf("user", "portfolio_profile", "read"),
            arrayOf("user", "portfolio_profile", "write"),
            arrayOf("admin", "portfolio_user", "read"),
            arrayOf("admin", "portfolio_user", "write"),
        )
        rbac.cas().addPolicies(policyAdding)
        return rbac.cas().policy
    }
    fun deleteUser(name: String): Boolean {
        return rbac.cas().deleteUser(name)
    }
    fun addAdminRole(id: String): List<String> {
        val roleAdding = DoubleRole(id, "admin")
        roleAdding.let { rbac.cas().addRoleForUser(it.memberRole, it.fatherRole) }
        return rbac.cas().getRolesForUser(id)
    }
    fun deleteAdminRole(id: String): List<String> {
        val roleDeleting = DoubleRole(id, "admin")
        roleDeleting.let { rbac.cas().deleteRoleForUser(it.memberRole, it.fatherRole) }
        return rbac.cas().getRolesForUser(id)
    }
}