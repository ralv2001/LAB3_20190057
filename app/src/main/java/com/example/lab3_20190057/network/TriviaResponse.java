package com.example.lab3_20190057.network;

import java.util.List;

public class TriviaResponse {
    private int response_code;
    private List<TriviaQuestion> results;

    public int getResponse_code() {
        return response_code;
    }

    public List<TriviaQuestion> getResults() {
        return results;
    }

    public static class TriviaQuestion {
        private String category;
        private String type;
        private String difficulty;
        private String question;
        private String correct_answer;
        private List<String> incorrect_answers;

        // Getters originales
        public String getCategory() {
            return category;
        }

        public String getType() {
            return type;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public String getQuestion() {
            return question;
        }

        public String getCorrect_answer() {
            return correct_answer;
        }

        public List<String> getIncorrect_answers() {
            return incorrect_answers;
        }

        // Setters para usar en el modo fallback
        public void setCategory(String category) {
            this.category = category;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public void setCorrect_answer(String correct_answer) {
            this.correct_answer = correct_answer;
        }

        public void setIncorrect_answers(List<String> incorrect_answers) {
            this.incorrect_answers = incorrect_answers;
        }
    }
}