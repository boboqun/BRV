package com.drake.brv.sample.model

import com.drake.brv.model.Item

class GroupModel : Item {

    override fun sublist(): List<Any?>? {
        return listOf(Model(), Model(), Model(), Model(), Model())
    }
}