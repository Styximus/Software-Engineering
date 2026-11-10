package de.quizmaster;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClozeTestQuestion extends Question{

    private String[] answers;

    public ClozeTestQuestion(int id, String questionText, String[] answers,int points) {
        super(id, questionText,points);
        this.answers = answers;
    }

    public String[] getAnswer() {
        return answers;
    }

    public void setAnswer(String[] answers) {
        this.answers = answers;
    }
    public int getGapCount() {
        Matcher matcher = Pattern.compile("\\{\\d+\\}").matcher(getQuestionText());
        int count = 0;
        while (matcher.find()){
            count++;
        }
        return  count;
    }
    @Override
    public boolean checkAnswer(String answer){
        String[] submittedAnswers = answer.split(";",-1);
        if (submittedAnswers.length != this.answers.length){
            return false;
        }

        for (int i =0; i< answers.length;i++){
            if(!submittedAnswers[i].trim().equalsIgnoreCase(answers[i].trim())){
                return  false;
            }
        }
        return true;
    }
    @Override
    public  QuestionType getType(){
        return QuestionType.CLOZE_TEST;
    }

}
