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
    }
}