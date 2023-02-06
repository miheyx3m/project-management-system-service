package com.foresightworks.projectmanagementsystemservice.data;

import com.foresightworks.projectmanagementsystemservice.db.models.Project;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AppData {

    @Getter
    private Map<String, Project> currentProjectMap;

    public void loadData() {
        currentProjectMap = new ConcurrentHashMap<>();
    }
}
