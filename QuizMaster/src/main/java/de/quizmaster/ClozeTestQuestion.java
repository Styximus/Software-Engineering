package de.quizmaster;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClozeTestQuestion extends Question{

    private String[] answers;

    public ClozeTestQuestion(int id, String questionText, String[] answers,int points) {
        super(id, questionText,points);
        this.answers = answers.clone();
    }

    public String[] getAnswer() {
        return answers.clone();
    }

    public void setAnswer(String[] answers) {
        this.answers = answers.clone();
    }
    /** First placeholder number, or -1 if the numbered fields cannot represent these answers. */
    public int firstGapNumber() {
        var numbers = new java.util.TreeSet<Integer>();
        Matcher matcher = Pattern.compile("\\{(\\d+)\\}").matcher(getQuestionText());
        try {
            while (matcher.find()) numbers.add(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException invalidNumber) {
            return -1;
        }
        if (numbers.isEmpty() || numbers.size() != answers.length) return -1;
        int first = numbers.first();
        return first <= 1 && numbers.last() - first == answers.length - 1 ? first : -1;
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
        if (answer == null) return false;
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
