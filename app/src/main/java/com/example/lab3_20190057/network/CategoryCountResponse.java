// Crear esta nueva clase en el paquete network
package com.example.lab3_20190057.network;

public class CategoryCountResponse {
    private CategoryCounts category_question_count;

    public CategoryCounts getCategory_question_count() {
        return category_question_count;
    }

    public static class CategoryCounts {
        private int total_question_count;
        private int total_easy_question_count;
        private int total_medium_question_count;
        private int total_hard_question_count;

        public int getTotal_question_count() {
            return total_question_count;
        }

        public int getTotal_easy_question_count() {
            return total_easy_question_count;
        }

        public int getTotal_medium_question_count() {
            return total_medium_question_count;
        }

        public int getTotal_hard_question_count() {
            return total_hard_question_count;
        }
    }
}