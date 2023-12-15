package com.example.DemoTelegramBot.services;

import com.example.DemoTelegramBot.models.Category;
import com.example.DemoTelegramBot.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findChildrenByParentId(Long parentId) {
        return categoryRepository.findChildrenByParentId(parentId);
    }

    public  List<Category> getTree() {
        return categoryRepository.getCategoryTree();
    }
}
