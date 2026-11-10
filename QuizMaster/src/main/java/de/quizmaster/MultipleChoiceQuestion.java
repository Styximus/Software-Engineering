package de.quizmaster;

import java.util.Arrays;

public class MultipleChoiceQuestion extends Question{
    
    private String[] options;
    private int[] answers;

    public MultipleChoiceQuestion(int id,String questionText,String[] options, int[] answers, int points) {
        super(id, questionText,points);
        this.options = options;
        this.answers = answers;
    }

    public String[] getOptions() {
        return options;
    }

    public int[] getAnswers() {

        return answers;
    }

    public void setAnswers(int[] answers) {

        this.answers = answers;
    }
    @Override
    public boolean checkAnswer(String answer){
        String[] selectedAnswers = answer.split(",");
        int[] selected = Arrays.stream(selectedAnswers).map(String::trim).mapToInt(Integer::parseInt).sorted().toArray();
        int [] correct = answers.clone();
        Arrays.sort(correct);
        return Arrays.equals(selected,correct);
    }
    @Override
    public QuestionType getType(){
        return QuestionType.MULTIPLE_CHOICE;
    }
}
