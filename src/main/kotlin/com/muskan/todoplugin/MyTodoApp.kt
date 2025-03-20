package com.muskan.todoplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.WrapLayout
import java.awt.*
import java.awt.event.*
import javax.swing.*

class MyTodoApp : ToolWindowFactory {

    private val taskListPanel = JPanel().apply {
        layout = WrapLayout(FlowLayout.LEFT, 10, 12) // Use WrapLayout for wrapping
        background = JBColor.WHITE
    }

    private val inputField = JBTextArea("Write your tasks here...").apply {
        font = Font("Arial", Font.PLAIN, 16)
        alignmentY = Component.CENTER_ALIGNMENT
        margin = JBUI.insets(10, 5)
        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                super.focusGained(e)
                if (text == "Write your tasks here...") {
                    text = ""
                }
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown) {
                        append("\n") // Shift + Enter adds a new line
                    } else {
                        e.consume() // Prevents moving to the next line
                        addTask()
                    }
                }
            }
        })
    }

    private val addButton = JButton("Add Task").apply {
        addActionListener { addTask() }
    }

    private fun addTask() {
        val taskText = inputField.text.trim()
        if (taskText.isEmpty()) return

        val taskPanel = JPanel().apply {
            layout = FlowLayout(FlowLayout.LEFT, 1, 5)
            maximumSize = Dimension(40, 50) // Set maximum width for task panel
        }

        val upButton = JButton("🔼").apply {
            isContentAreaFilled = false
            isBorderPainted = false
            preferredSize = Dimension(40, 30)
            toolTipText = "Move Up"
            addActionListener { moveTaskUp(taskPanel) }
        }

        val downButton = JButton("🔽").apply {
            isContentAreaFilled = false
            isBorderPainted = false
            preferredSize = Dimension(40, 30)
            toolTipText = "Move Down"
            addActionListener { moveTaskDown(taskPanel) }
        }

        val dustbinIcon = ImageIcon(
            ImageIcon(javaClass.getResource("/icons/delete.png"))
                .image.getScaledInstance(18, 18, Image.SCALE_SMOOTH) // Scale icon
        )
        val checkedIcon = ImageIcon(
            ImageIcon(javaClass.getResource("/icons/square.png"))
                .image.getScaledInstance(18, 18, Image.SCALE_SMOOTH) // Scale icon
        )

        // Keep it outside if called inside it will not work
        val uncheckedIcon = ImageIcon(
            ImageIcon(javaClass.getResource("/icons/unchecked.png"))
                .image.getScaledInstance(18, 18, Image.SCALE_SMOOTH) // Scale icon
        )

        val deleteButton = JButton(dustbinIcon).apply {
            isContentAreaFilled = false  // Remove button background
            isBorderPainted = false  // Remove button border
            preferredSize = Dimension(30, 30) // Proper size
            toolTipText = "Delete Task"
            addActionListener { deleteTask(taskPanel) }
        }

        val checkBoxLabel = JLabel().apply {
            try {
                icon = uncheckedIcon
                size = Dimension(30, 30)
                text = taskText
                font = Font("Arial", Font.PLAIN, 16)
                iconTextGap = 6
                horizontalTextPosition = SwingConstants.RIGHT // Align text to the right of the icon

                addMouseListener(object : MouseAdapter() {
                    var isChecked = false

                    override fun mouseClicked(e: MouseEvent?) {
                        isChecked = !isChecked
                        icon = if (isChecked) checkedIcon else uncheckedIcon
                    }
                })
            } catch (e: Exception) {
                println("Error: Image not found at $e")
            }
        }

        taskPanel.add(checkBoxLabel)
        taskPanel.add(upButton)
        taskPanel.add(downButton)
        taskPanel.add(deleteButton)
        taskListPanel.add(taskPanel)
        taskListPanel.revalidate()
        taskListPanel.repaint()
        inputField.text = ""
    }

    private fun moveTaskUp(taskPanel: JPanel) {
        val index = taskListPanel.components.indexOf(taskPanel)
        if (index > 0) {
            taskListPanel.remove(taskPanel)
            taskListPanel.add(taskPanel, index - 1)
            taskListPanel.revalidate()
            taskListPanel.repaint()
        }
    }

    private fun moveTaskDown(taskPanel: JPanel) {
        val index = taskListPanel.components.indexOf(taskPanel)
        if (index < taskListPanel.componentCount - 1) {
            taskListPanel.remove(taskPanel)
            taskListPanel.add(taskPanel, index + 1)
            taskListPanel.revalidate()
            taskListPanel.repaint()
        }
    }

    private fun deleteTask(taskPanel: JPanel) {
        taskListPanel.remove(taskPanel)
        taskListPanel.revalidate()
        taskListPanel.repaint()
    }

    private val inputPanel = JPanel(BorderLayout()).apply {
        add(inputField, BorderLayout.CENTER)
        add(addButton, BorderLayout.EAST)
    }

    private val scrollPane =
        JBScrollPane(taskListPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    private val mainPanel = JPanel(BorderLayout()).apply {
        add(inputPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}