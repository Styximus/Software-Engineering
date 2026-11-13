package de.quizmaster;

/** Demonstration content; production callers supply their own Npc and Quiz. */
final class DemoQuizzes {
    static Npc biology() {
        Quiz quiz = new Quiz(1, new Question[]{
            new MultipleChoiceQuestion(101, "Was brauchen Pflanzen für die Fotosynthese?",
                new String[]{"Sonnenlicht", "Wasser", "Kohlenstoffdioxid", "Sand"}, new int[]{0, 1, 2}, 3),
            new TrueFalseQuestion(102, "Delfine gehören zu den Säugetieren.", true, 1),
            new TextQuestion(103, "Wie heißt der grüne Farbstoff in den Blättern?", "Chlorophyll", 2),
            new ClozeTestQuestion(104, "Bei der Fotosynthese nehmen Pflanzen {1} auf und geben {2} ab.",
                new String[]{"Kohlenstoffdioxid", "Sauerstoff"}, 2),
            new MultipleChoiceQuestion(105, "Welcher Baum trägt Eicheln?",
                new String[]{"Die Buche", "Die Eiche", "Die Birke", "Die Fichte"}, new int[]{1}, 2)
        });
        return new Npc(1, new Position(0, 0), "Deine Biologie-Expertin",
            "„Schön, dass du da bist! Wusstest du, dass die Mitochondrien die Kraftwerke der Zelle sind?“",
            "Mila", QuizTheme.image("mila"), quiz);
    }
}
