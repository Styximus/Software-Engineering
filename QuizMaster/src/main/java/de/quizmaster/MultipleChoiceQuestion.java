package de.quizmaster;

import java.util.Arrays;

public class MultipleChoiceQuestion extends Question{
    
    private String[] options;
    private int[] answers;

    public MultipleChoiceQuestion(int id,String questionText,String[] options, int[] answers, int points) {
        super(id, questionText,points);
        this.options = options.clone();
        this.answers = answers.clone();
    }

    public String[] getOptions() {
        return options.clone();
    }

    public int[] getAnswers() {

        return answers.clone();
    }

    public void setAnswers(int[] answers) {

        this.answers = answers.clone();
    }
    @Override
    public boolean checkAnswer(String answer){
        if (answer == null || answer.isBlank()) return false;
        try {
            String[] selectedAnswers = answer.split(",", -1);
            int[] selected = Arrays.stream(selectedAnswers).map(String::trim).mapToInt(Integer::parseInt).sorted().toArray();
            int[] correct = answers.clone();
            Arrays.sort(correct);
            return Arrays.equals(selected, correct);
        } catch (NumberFormatException invalidInput) {
            return false;
        }
    }
    @Override
    public QuestionType getType(){
        return QuestionType.MULTIPLE_CHOICE;
    }
}
