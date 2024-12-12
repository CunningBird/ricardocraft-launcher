package ru.ricardocraft.client.utils.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Category {

    public final BaseCommandCategory category;
    public final String name;
    public String description;

    @Autowired
    public Category(BaseCommandCategory category, CommandHandler commandHandler) {
        this.category = category;
        this.name = "runtime";
        commandHandler.registerCategory(this);
    }
}