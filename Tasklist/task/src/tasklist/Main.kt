package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File

class Task {

    var description: MutableList<String> = mutableListOf()
    var priority: String = "L"
    var date: String = ""
    var time: String = "00:00"
    var dueTag: String = "I"

    fun getDueTag() {
        val taskDate = LocalDate.parse(date)
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        if (numberOfDays == 0) {
            dueTag = "T"
        } else if (numberOfDays > 0) {
            dueTag = "I"
        } else {
            dueTag = "O"
        }
    }
}

fun main() {

    val jsonFile = File("tasklist.json")
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val type = Types.newParameterizedType(List::class.java, Task:: class.java)
    val adapter = moshi.adapter<List<Task>>(type)
    var tasks = mutableListOf<Task>()
    if(jsonFile.exists()) {
        val jsonStr = jsonFile.readText();
        val taskList = adapter.fromJson(jsonStr)
        if(taskList != null) {
            tasks = taskList as MutableList<Task>
        }
    }

    var read = true
    while(read) {
        val menu = readMenu()
        when (menu.lowercase()) {
            "add" -> {
                val task = readTask()
                if(task != null) {
                    tasks.add(task)
                }
            }
            "print" -> {
                printTasks(tasks)
            }
            "edit" -> {
                printTasks(tasks)
                if(tasks.isNotEmpty()) {
                    var index = printTaskDelete(tasks.size)
                    if(index != null) {
                        index--
                        val editOption = readEditOption();
                        when (editOption) {
                            "priority" -> {
                                val priority = readTaskPriority()
                                tasks[index].priority = priority
                            }
                            "date" -> {
                                val date = readDate()
                                tasks[index].date = date
                            }
                            "time" -> {
                                val time = readTime()
                                tasks[index].time = time
                            }
                            "task" -> {
                                val desc = readTaskDescription()
                                tasks[index].description = desc
                            }
                        }
                        println("The task is changed")
                    }
                }
            }
            "delete" -> {
                printTasks(tasks)
                if(tasks.isNotEmpty()) {
                    var index: Int? = printTaskDelete(tasks.size)
                    if (index != null) {
                        index--
                        deleteTask(index, tasks)
                    }
                }
            }
            "end" -> {
                val jsonStr = adapter.toJson(tasks)
                jsonFile.writeText(jsonStr)
                println("Tasklist exiting!")
                read = false
            }
            else -> {
                println("The input action is invalid")
            }
         }
    }
}

fun readMenu(): String {
    println("Input an action (add, print, edit, delete, end):")
    return readln()
}

fun printTaskDelete(numTasks: Int): Int? {
    var input: Int = 0
    var read = true
    while (read) {
        println("Input the task number (1-$numTasks):")
        try {
            input = readln().toInt()
        } catch (e: Exception) {
            println("Invalid task number")
            continue;
        }
        if (input > numTasks || input < 1) {
            println("Invalid task number")
        } else {
            read = false
        }
    }
    return input;
}

fun deleteTask(index: Int, tasks: MutableList<Task>) {
    tasks.removeAt(index);
    println("The task is deleted")
}

fun readEditOption(): String {
    var option = ""
    var read = true
    while(read) {
        println("Input a field to edit (priority, date, time, task):")
        val input = readln().lowercase()
        if(input == "priority" || input == "date" || input == "time" || input == "task") {
            option = input
            read = false
        } else {
            println("Invalid field")
        }
    }
    return option
}

fun readTask(): Task? {
    val priority = readTaskPriority()
    val date = readDate()
    val time = readTime()
    val desc = readTaskDescription()
    val task = Task()
    task.priority = priority
    task.date = date
    task.time = time
    task.getDueTag()
    if (desc.isNotEmpty()) {
        task.description = desc
        return task
    } else {
        println("The task is blank")
        return null
    }

}

fun readTaskPriority(): String {
    var read = true
    var priority = ""
    while(read) {
        println("Input the task priority (C, H, N, L):")
        priority = readln().uppercase()
        if(priority == "C" || priority == "H" ||
            priority == "N" || priority == "L") {
            read = false
        }
    }
    return priority
}

fun readDate() : String {
    var read = true
    var date = ""
    while(read) {
        println("Input the date (yyyy-mm-dd):")
        date = readln()
        val input = date.split("-");
        if(input.size == 3) {
            val year = input[0]
            val month = addZero(input[1])
            val day = addZero(input[2])
            date = "${year}-${month}-${day}"
            try {
                //val instant = Instant.parse(date)
                val date = LocalDate.parse(date)
                read = false
            } catch (e: Exception) {
                println("The input date is invalid")
            }
        } else {
            println("The input date is invalid")
        }

    }
    return date
}

fun addZero(text: String): String {
    return if(text.length == 1) "0$text" else text
}

fun readTime(): String {
    var read = true
    var time = ""
    while(read) {
        println("Input the time (hh:mm):")
        time = readln()
        val valid = isTimeValid(time)
        if(valid) {
            read = false
        } else {
            println("The input time is invalid")
        }
    }
    time = formatTime(time);
    return time
}

fun formatTime(time: String) : String {
    val components = time.split(":")
    val hour = addZero(components[0])
    val min = addZero(components[1])
    return "${hour}:${min}"
}

fun isTimeValid(time: String): Boolean {
    if(time.contains(":")) {
        val components = time.split(":")
        if(components.size == 2) {
            if (components[0].toInt() <= 23) {
               if(components[1].toInt() <= 59) {
                   return true
               }
            }
        }
    }
    return false
}

fun readTaskDescription(): MutableList<String> {
    val description = mutableListOf<String>()
    var read = true
    println("Input a new task (enter a blank line to end):")
    while(read) {
        val taskLine = readln().trim()
        if(taskLine == "" || taskLine == null) {
            read = false
        } else {
            description.add(taskLine)
        }
    }
    return description
}

fun printTasks(tasks: List<Task>) {
    if(tasks.isEmpty()) {
        println("No tasks have been input")
        return
    }
    var taskString = "${getSeparator()}\n${getHeading()}\n${getSeparator()}\n"
    taskString += ""
    for(i in tasks.indices) {
        val descString = getDescriptionString(tasks[i].description);
        for(j in 0..descString.lastIndex) {
            if(j == 0) {
                taskString += "| ${i+1}${getSpaceForNum(i+1)}|"
                taskString += " ${tasks[i].date} | ${tasks[i].time} |"
                taskString += " ${getPriorityString(tasks[i].priority)} |"
                taskString += " ${getDueString(tasks[i].dueTag)} |"
            } else {
                taskString += "|${getSpaces(4)}|"
                taskString += "${getSpaces(12)}|"
                taskString += "${getSpaces(7)}|"
                taskString += "${getSpaces(3)}|"
                taskString += "${getSpaces(3)}|"
            }
            val descSpaces = getSpaces(44 - descString[j].length)
            taskString += "${descString[j]}${descSpaces}|\n"
        }
        taskString += "${getSeparator()}\n"
    }
    println(taskString)
}

fun getDescriptionString(desc: List<String>): MutableList<String> {
    val descString = mutableListOf<String>()
    for(value in desc) {
        val q = value.length / 44
        val r = value.length % 44
        for(i in 0..q) {
            if(i != q) {
                descString.add(value.subSequence(i * 44, (i + 1) * 44).toString())
            } else {
                if( r != 0) {
                    descString.add(value.subSequence(i * 44, ((i * 44) + r)).toString())
                }
            }
        }
    }
    return descString
}

fun getSpaceForNum(num: Int): String {
    if(num >= 10) {
        return getSpaces(1)
    } else {
        return getSpaces(2)
    }
}

fun printTask(index: Int, task: Task) {
    var taskString = ""
    taskString
    if(index > 8) {
        println("${index+1} ${task.date} ${task.time} ${task.priority.uppercase()} ${task.dueTag.uppercase()}")
    } else {
        println("${index+1}  ${task.date} ${task.time} ${task.priority.uppercase()} ${task.dueTag.uppercase()}")
    }
    for(i in 0..task.description.lastIndex) {
        println("   ${task.description[i]}")
    }
}



fun getHeading(): String {
    return "| N  |    Date    | Time  | P | D |${getSpaces(19)}Task${getSpaces(21)}|"
}

fun getDueString(due: String) : String {
    var dueString = ""
    if(due == "I") {
        dueString += "\u001B[102m \u001B[0m"
    } else if (due == "T") {
        dueString += "\u001B[103m \u001B[0m"
    } else if (due == "O") {
        dueString += "\u001B[101m \u001B[0m"
    }
    return dueString
}

fun getPriorityString(priority: String): String {
    var priorityString = ""
    if(priority == "C") {
        priorityString = "\u001B[101m \u001B[0m"
    } else if (priority == "H") {
        priorityString = "\u001B[103m \u001B[0m"
    } else if (priority == "N") {
        priorityString = "\u001B[102m \u001B[0m"
    } else if (priority == "L") {
        priorityString = "\u001B[104m \u001B[0m"
    }
    return priorityString
}

fun getSeparator(): String {
    var separator = "+"
    separator += getDash(4)
    separator += "+"
    separator += getDash(12)
    separator += "+"
    separator += getDash(7)
    separator += "+"
    separator += getDash(3)
    separator += "+"
    separator += getDash(3)
    separator += "+"
    separator += getDash(44)
    separator += "+"
    return separator
}

fun getDash(num: Int): String {
    var string = ""
    for(i in 1..num) {
        string += "-"
    }
    return string
}

fun getSpaces(num: Int): String {
    var string = ""
    for(i in 1..num) {
        string += " "
    }
    return string
}


