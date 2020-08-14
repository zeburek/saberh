package ru.zeburek.saberh.utils

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region


/**
 * [DragResizer] can be used to add mouse listeners to a [Region]
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 *
 *
 * Height and Width resizing is working (hopefully) properly
 *
 * <pre>
 * DragResizer.makeResizable(myAnchorPane);
</pre> *
 *
 * @author Cannibalsticky (modified from the original DragResizer created by AndyTill)
 */
class DragResizer private constructor(private val region: Region) {
    private var y: Double = 0.0
    private var x: Double = 0.0
    private var initMinHeight = false
    private var initMinWidth = false
    private var draggableZoneX = false
    private var draggableZoneY = false
    private var dragging = false

    protected fun mouseReleased(event: MouseEvent?) {
        dragging = false
        region.cursor = Cursor.DEFAULT
    }

    protected fun mouseOver(event: MouseEvent?) {
        if (isInDraggableZone(event) || dragging) {
            if (draggableZoneY) {
                region.cursor = Cursor.S_RESIZE
            }
            if (draggableZoneX) {
                region.cursor = Cursor.E_RESIZE
            }
        } else {
            region.cursor = Cursor.DEFAULT
        }
    }

    //had to use 2 variables for the controll, tried without, had unexpected behaviour (going big was ok, going small nope.)
    protected fun isInDraggableZone(event: MouseEvent?): Boolean {
        if (event == null) return false
        draggableZoneY = (event.y > region.height - RESIZE_MARGIN)
        draggableZoneX = (event.x > region.width - RESIZE_MARGIN)
        return draggableZoneY || draggableZoneX
    }

    protected fun mouseDragged(event: MouseEvent?) {
        if (!dragging || event == null) {
            return
        }
        if (draggableZoneY) {
            val mousey = event.y
            val newHeight = region.minHeight + (mousey - y)
            region.minHeight = newHeight
            y = mousey
        }
        if (draggableZoneX) {
            val mousex = event.x
            val newWidth = region.minWidth + (mousex - x)
            region.minWidth = newWidth
            x = mousex
        }
    }

    protected fun mousePressed(event: MouseEvent?) {

        // ignore clicks outside of the draggable margin
        if (!isInDraggableZone(event) || event == null) {
            return
        }
        dragging = true

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinHeight) {
            region.minHeight = region.height
            initMinHeight = true
        }
        y = event.y
        if (!initMinWidth) {
            region.minWidth = region.width
            initMinWidth = true
        }
        x = event.x
    }

    companion object {
        /**
         * The margin around the control that a user can click in to start resizing
         * the region.
         */
        private const val RESIZE_MARGIN = 10
        fun makeResizable(region: Region) {
            val resizer = DragResizer(region)
            region.onMousePressed = object : EventHandler<MouseEvent?> {
                override fun handle(event: MouseEvent?) {
                    resizer.mousePressed(event)
                }
            }
            region.onMouseDragged = object : EventHandler<MouseEvent?> {
                override fun handle(event: MouseEvent?) {
                    resizer.mouseDragged(event)
                }
            }
            region.onMouseMoved = object : EventHandler<MouseEvent?> {
                override fun handle(event: MouseEvent?) {
                    resizer.mouseOver(event)
                }
            }
            region.onMouseReleased = object : EventHandler<MouseEvent?> {
                override fun handle(event: MouseEvent?) {
                    resizer.mouseReleased(event)
                }
            }
        }
    }

}