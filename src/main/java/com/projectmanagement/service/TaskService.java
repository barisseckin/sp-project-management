package com.projectmanagement.service;

import com.projectmanagement.dto.TaskDto;
import com.projectmanagement.dto.converter.TaskDtoConverter;
import com.projectmanagement.dto.request.TaskCreateRequest;
import com.projectmanagement.dto.request.UpdateTaskRequest;
import com.projectmanagement.model.Task;
import com.projectmanagement.repository.TaskRepository;
import com.projectmanagement.utils.MailSendService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskDtoConverter taskDtoConverter;

    private final UserService userService;

    private final MailSendService mailSendService;

    public TaskDto save(TaskCreateRequest request) {
        Task task = new Task();
        task.setId(UUID.randomUUID().toString());
        task.setCode(RandomUtils.nextInt(1, 1000));
        task.setCreateDate(LocalDate.now());
        task.setBody(request.getBody());
        task.setTitle(request.getTitle());
        task.setPriorityType(request.getPriorityType());
        task.setCreatedBy(request.getCreatedBy());

        if (userService.getActiveStatus(request.getAssignee())) {
            task.setAssignee(userService.getByUser(request.getAssignee()));

            taskRepository.save(task);
            mailSendService.sendMail(userService.getByUser(request.getAssignee()).getMail(), "[YouTrack, Assigned] Issue " + task.getCode(), "" + task.getBody());
            return taskDtoConverter.convert(task);
        }

        return null;
    }

    public List<TaskDto> getAll() {
        return taskDtoConverter.convert(taskRepository.findAll());
    }

    public void delete(int code) {
        Task task = taskRepository.findTaskByCode(code);
        taskRepository.deleteById(task.getId());
    }

    public TaskDto getByCode(int code) {
        return taskDtoConverter.convert(taskRepository.findTaskByCode(code));
    }

    public TaskDto update(int code, UpdateTaskRequest request){
        Task task = taskRepository.findTaskByCode(code);

        Task updatedTask = new Task();
        updatedTask.setCode(task.getCode());
        updatedTask.setUpdateDate(LocalDate.now());
        updatedTask.setId(task.getId());
        updatedTask.setTitle(request.getTitle());
        updatedTask.setBody(request.getBody());
        updatedTask.setPriorityType(request.getPriorityType());
        updatedTask.setCreatedBy(task.getCreatedBy());
        updatedTask.setCreateDate(task.getCreateDate());

        if (userService.getActiveStatus(request.getAssignee())) {
            updatedTask.setAssignee(userService.getByUser(request.getAssignee()));

            return taskDtoConverter.convert(taskRepository.save(updatedTask));
        }

        return null;
    }

}
