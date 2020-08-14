package ru.zeburek.saberh.utils

import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class ActionObservableList<T>(p0: ObservableList<T>) : SimpleListProperty<T>(p0) {
    private val onAddCallbacks: ArrayList<(element: T) -> T> = arrayListOf()

    fun onAdd(callback: (element: T) -> T) {
        onAddCallbacks.add(callback)
    }

    override fun add(element: T): Boolean {
        var value = element
        onAddCallbacks.forEach { value = it(element) }
        return super.add(value)
    }

    companion object {
        fun <E>create(): ActionObservableList<E>{
            return ActionObservableList(FXCollections.observableArrayList())
        }
    }
}