package de.uni_hildesheim.sse.exerciseLib;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.uni_hildesheim.sse.exerciseReviewer.core.ReviewPublicMessage;
import de.uni_hildesheim.sse.exerciseSubmitter.Activator;
import de.uni_hildesheim.sse.exerciseSubmitter.submission.
    CommunicationException;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assessment;
import net.ssehub.exercisesubmitter.protocol.frontend.ExerciseReviewerProtocol;

/**
 * Defines an internal data structure for keeping exercises.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class ExerciseData {

    /**
     * Stores the text formatter for credits.
     * 
     * @since 1.00
     */
    private static final NumberFormat CREDIT_FORMATTER 
        = new DecimalFormat("0.0");
    
    /**
     * Defines the separator string (tabulator).
     * 
     * @since 1.00
     */
    private static final String SEPARATOR = "\t";
    
    /**
     * Stores the review comment denoting an empty review.
     * 
     * @since 1.00
     */
    private static final String EMPTY_REVIEW = "-";
    
    /**
     * Stores the exercises assigned to their names/identifications.
     * 
     * @since 1.00
     */
    private Map<String, Exercise> exercises =
        new HashMap<String, Exercise>();

    /**
     * Stores the user data provider.
     * 
     * @since 1.10
     */
    private UserProvider provider;
    
    /**
     * Creates a new exercise data object.
     * 
     * @param provider the user data provider
     * 
     * @since 1.10
     */
    public ExerciseData(UserProvider provider) {
        this.provider = provider;
    }
    
    /**
     * Returns all exercises.
     * 
     * @return all exercises as an iterator
     * 
     * @since 1.10
     */
    public Iterator<Exercise> exercises() {
        return exercises.values().iterator();
    }
    
    /**
     * Merges this exercise data object with the given one.
     * 
     * @param externalData the data to be merged
     * @return <code>true</code> if data was merged, 
     *         <code>false</code> else
     * @throws CommunicationException if any error occurred
     * 
     * @since 1.08
     */
    public boolean mergeWith(ExerciseData externalData) 
        throws CommunicationException {
        boolean merged = false;
        List<RealUser> allUsers = provider.getAllKnownUsers();
        Map<String, Object> allGroups = new HashMap<String, Object>();
        for (String group : RealUser.getAllGroups(allUsers)) {
            allGroups.put(group, null);
        }
        for (Iterator<Map.Entry<String, Exercise>> iter = 
                externalData.exercises.entrySet().iterator(); 
            iter.hasNext();) {
            Map.Entry<String, Exercise> entry = iter.next();
            Exercise sourceExercise = entry.getValue();
            if (!exercises.containsKey(sourceExercise.getName())) {
                // merge complete exercise with not-existing one
                
                // cleanup unknown groups
                for (Iterator<Map.Entry<String, Review>> rIter = 
                        sourceExercise.userReviewMappings(); 
                    rIter.hasNext();) {
                    Map.Entry<String, Review> rEntry = rIter.next();
                    Review review = rEntry.getValue();
                    if (!allGroups.containsKey(review.getUserName())) {
                        rIter.remove();
                    }
                }

                // add exercise
                if (sourceExercise.getReviewCount() > 0) {
                    addExercise(sourceExercise);
                    merged = true;
                }
            } else {
                // merge partial exercise with existing one

                Exercise targetExercise = exercises.get(
                    sourceExercise.getName());
                for (Iterator<Map.Entry<String, Review>> rIter = 
                        sourceExercise.userReviewMappings(); 
                    rIter.hasNext();) {
                    Map.Entry<String, Review> rEntry = rIter.next();
                    Review review = rEntry.getValue();
                    if (allGroups.containsKey(review.getUserName()) 
                        && null == targetExercise.getReview(
                            review.getUserName())) {
                        targetExercise.addReview(review);
                        merged = true;
                    }
                }
            }
        }
        return merged;
    }

    /**
     * Loads the exercise data structure from the tabulator-separated 
     * file.<br/>
     * Structure:<br/> 
     * User task1 review submitted task2 review submitted ... 
     * *max*  4     4        4       5      5      5 
     * user1  1    bad       5      ok      2     ok
     * 
     * @param in
     *            the input reader
     * @throws IOException
     *             if input/output problems occur
     * @throws CommunicationException
     *             wrapped exceptions while reading
     * 
     * @since 1.00
     */
    public void load(Reader in) throws IOException, CommunicationException {
        load(in, true); 
    }
    
    /**
     * Loads the exercise data structure from the tabulator-separated 
     * file.<br/>
     * Structure:<br/> 
     * User task1 review submitted task2 review submitted ... 
     * *max*  4     4        4       5      5      5 
     * user1  1    bad       5      ok      2     ok
     * 
     * @param in
     *            the input reader
     * @param throwOnMissingUser 
     *            should an exception be thrown when an user cannot be found
     * @throws IOException
     *             if input/output problems occur
     * @throws CommunicationException
     *             wrapped exceptions while reading
     * 
     * @since 1.00
     */
    public void load(Reader in, boolean throwOnMissingUser) 
        throws IOException, CommunicationException {
        LineNumberReader reader = new LineNumberReader(in);
        String line; 
        List<String> exerciseName = new ArrayList<String>();
        do {
            line = reader.readLine();
            if (null != line) {
                StringTokenizer tokenizer =
                    new StringTokenizer(line, SEPARATOR);
                int lineNr = reader.getLineNumber();
                if (1 == reader.getLineNumber()) {
                    readFirstLine(tokenizer, exerciseName, lineNr);
                } else if (2 == reader.getLineNumber()) {
                    readSecondLine(tokenizer, exerciseName, lineNr);
                } else {
                    readOtherLines(tokenizer, exerciseName, lineNr, 
                        throwOnMissingUser);
                }
            }
        } while (null != line);
        reader.close();
    }

    /**
     * Reads the first line in the exercise table. Called by
     * {@link #load(Reader)}.
     * 
     * @param tokenizer
     *            the current line split up by {@link #SEPARATOR}
     * @param exerciseName
     *            the exercise names to be modified as a side effect in this
     *            method
     * @param lineNr the current line lumber
     * 
     * @since 1.11
     */
    private void readFirstLine(StringTokenizer tokenizer,
        List<String> exerciseName, int lineNr) {
        if (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            while (tokenizer.hasMoreTokens()) {
                exerciseName.add(tokenizer.nextToken());
                if (tokenizer.hasMoreTokens()) {
                    // ignore review text column
                    tokenizer.nextToken();
                }
                if (tokenizer.hasMoreTokens()) {
                    // ignore review submitted column
                    tokenizer.nextToken();
                }
            }
        }
    }

    /**
     * Reads the second line in the exercise table. Called by
     * {@link #load(Reader)}.
     * 
     * @param tokenizer
     *            the current line split up by {@link #SEPARATOR}
     * @param exerciseName
     *            the exercise names
     * @param lineNr the current line lumber
     * 
     * @throws CommunicationException
     *             wrapped exceptions while reading
     * 
     * @since 1.11
     */
    private void readSecondLine(StringTokenizer tokenizer,
        List<String> exerciseName, int lineNr) 
        throws CommunicationException {
        if (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            int pos = 0;
            while (tokenizer.hasMoreTokens()) {
                try {
                    Exercise ex =
                        new Exercise(exerciseName.get(pos), 
                            readCredits(tokenizer.nextToken()));
                    addExercise(ex);
                } catch (NumberFormatException nfe) {
                    throw createException(
                        ReviewPublicMessage.INVALID_SYNTAX, 
                        nfe, lineNr);
                } catch (ArrayIndexOutOfBoundsException ae) {
                    throw createException(
                        ReviewPublicMessage.INVALID_SYNTAX, 
                        ae, lineNr);
                }
                if (tokenizer.hasMoreTokens()) {
                    // ignore review text column
                    tokenizer.nextToken();
                }
                if (tokenizer.hasMoreTokens()) {
                    // ignore review submitted column
                    tokenizer.nextToken();
                }
                pos++;
            }
        }
    }

    /**
     * Reads the other lines in the exercise table. Called by
     * {@link #load(Reader)}.
     * 
     * @param tokenizer
     *            the current line split up by {@link #SEPARATOR}
     * @param exerciseName
     *            the exercise names
     * @param lineNr the current line lumber
     * @param throwOnMissingUser 
     *            should an exception be thrown when an user cannot be found
     * 
     * @throws CommunicationException
     *             wrapped exceptions while reading
     * 
     * @since 1.11
     */
    private void readOtherLines(StringTokenizer tokenizer,
        List<String> exerciseName, int lineNr, boolean throwOnMissingUser) 
        throws CommunicationException {
        if (tokenizer.hasMoreTokens()) {
            String user = tokenizer.nextToken();
            if (null == provider.getSubmissionUser(user)) {
                if (throwOnMissingUser) {
                    throw new ReviewException(
                        ReviewPublicMessage.NO_USER_FOUND, 
                        new Throwable(), user);
                }
            } else {
                int pos = 0;
                while (tokenizer.hasMoreTokens()) {
                    try {
                        Exercise ex = getExercise(exerciseName.get(pos));
                        double credits =
                            readCredits(tokenizer.nextToken());
                        if (tokenizer.hasMoreTokens()) {
                            considerReview(tokenizer, user, credits, ex);
                        }
                    } catch (NumberFormatException nfe) {
                        throw createException(
                            ReviewPublicMessage.INVALID_SYNTAX, 
                            nfe, lineNr);
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        throw createException(
                            ReviewPublicMessage.INVALID_SYNTAX, 
                            ae, lineNr);
                    }
                    pos++;
                }
            }
        }
    }
    
    /**
     * Creates a new reviewer exception for a dedicated line number.
     * 
     * @param message the public message for the user
     * @param throwable an optional exception which indicates the 
     *        internal error reason
     * @param lineNr the line number usually in the reviewer data file
     * @return the created exception
     * 
     * @since 1.20
     */
    private static ReviewException createException(
        CommunicationException.PublicMessage message, 
        Throwable throwable, int lineNr) {
        return new ReviewException(message, throwable, "line " + lineNr);
    }

    /**
     * Considers a review for creation while reading.
     * 
     * @param tokenizer the tokenizer of the current line
     * @param user the user name of the currently handled user
     * @param credits the number of credits read before
     * @param ex the currently considered exercise
     * 
     * @since 1.00
     */
    private void considerReview(StringTokenizer tokenizer, String user, double credits, Exercise ex) {
        Assessment assessment = null;
        try {
            // Not nice here, but the tool uses only one protocol instance -> Thus, it can be used that way
            assessment = ((ExerciseReviewerProtocol) Activator.getProtocol()).getAssessmentForSubmission(user);
        } catch (NetworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null != assessment) {
            Review review = new Review(assessment);
            ex.addReview(review);
        }
        
//        String rev = unmask(tokenizer.nextToken());
//        if (tokenizer.hasMoreTokens()) {
//            String submitted = tokenizer.nextToken();
//            if (!EMPTY_REVIEW.equals(rev)) {
//                Review review =
//                    new Review(assessment);
//                if (Boolean.parseBoolean(submitted)) {
//                    review.setSubmittedToServer();
//                }
//                ex.addReview(review);
//            }
//        }
    }
    
    /**
     * Masks the given string so that it can be stored
     * in a tabulator-separated file.
     * 
     * @param string the string to be masked
     * @return the masked string
     * 
     * @since 1.00
     */
    private String mask(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            switch (c) {
            case '\n':
                builder.append("<*n*>");
                break;
            case '\r':
                builder.append("<*r*>");
                break;
            case '\t':
                builder.append("<*t*>");
                break;
            default:
                builder.append(c);
                break;
            }
        }
        String result = builder.toString();
        if (0 == result.length()) {
            result = EMPTY_REVIEW;
        }
        return result;
    }

    /**
     * Unmasks the given string so that a string that
     * was stored in a tabulator-separated file can
     * be displayed without additional substrings 
     * introduced by {@link #mask(String)}.
     * 
     * @param string the string to be unmasked
     * @return the unmasked string
     * 
     * @since 1.00
     */
    private String unmask(String string) {
        string = string.replace("<*t*>", "\t");
        string = string.replace("<*n*>", "\n");
        string = string.replace("<*r*>", "\r");
        return string;
    }

    /**
     * Writes out this data structure.
     * 
     * @param out
     *            the output writer
     * @param realUsers
     *            <code>true</code> write each real user in one
     *            line, <code>false</code> write lines for users 
     *            (user groups) only
     * @throws IOException
     *             if input/output problems occur
     * 
     * @since 1.00
     */
    public void store(Writer out, boolean realUsers) throws IOException {
        PrintWriter writer = new PrintWriter(out);

        List<Exercise> exerciseList = new ArrayList<Exercise>();
        for (Exercise ex : exercises.values()) {
            exerciseList.add(ex);
        }
        Collections.sort(exerciseList);

        writer.print("user");
        writer.print(SEPARATOR);
        if (realUsers) {
            writer.print("account");
            writer.print(SEPARATOR);
        }
        for (Exercise exercise : exerciseList) {
            writer.print(exercise.getName());
            writer.print(SEPARATOR);
            // review text column
            writer.print(exercise.getName());
            writer.print(SEPARATOR);
            // review submitted column
            writer.print(exercise.getName());
            writer.print(SEPARATOR);
        }
        writer.println();

        writer.print("*max*");
        writer.print(SEPARATOR);
        if (realUsers) {
            writer.print(SEPARATOR);
        }
        for (Exercise exercise : exerciseList) {
            writer.print(formatCredits(exercise.getMaxCredits()));
            writer.print(SEPARATOR);
            // review text column
            writer.print(formatCredits(exercise.getMaxCredits()));
            writer.print(SEPARATOR);
            // review submitted column
            writer.print(formatCredits(exercise.getMaxCredits()));
            writer.print(SEPARATOR);
        }
        writer.println();

        List<User> userList = new ArrayList<User>();
        List<RealUser> realUserList = new ArrayList<RealUser>();
        Map<RealUser, User> realUserMap = new HashMap<RealUser, User>();
        for (User user : provider.submissionUsers()) {
            userList.add(user);
            if (realUsers) {
                for (Iterator<RealUser> iter = user.getRealUsers();
                    iter.hasNext();) {
                    RealUser ru = iter.next();
                    realUserList.add(ru);
                    realUserMap.put(ru, user);
                }
            }
        }

        if (realUsers) {
            Collections.sort(realUserList);
            for (RealUser realUser : realUserList) {
                User user = realUserMap.get(realUser);
                writer.print(realUser.getName());
                writer.print(SEPARATOR);
                writeSafe(writer, realUser.getSystemAccount());
                storeExercises(writer, exerciseList, user);
            }
        } else {
            Collections.sort(userList);
            for (User user : userList) {
                writer.print(user.getUserName());
                storeExercises(writer, exerciseList, user);
            }
        }

        writer.close();
    }
    
    /**
     * Writes a string to <code>writer</code> if <code>string</code> is not 
     * <b>null</b>.
     * 
     * @param writer the writer to write to
     * @param string the string to write
     */
    private void writeSafe(PrintWriter writer, String string) {
        if (null != string) {
            writer.print(string);
        }
    }

    /**
     * Formats credits for output.
     * 
     * @param credits the credits to be formatted
     * @return the formatted credits
     * 
     * @since 1.00
     */
    public static final String formatCredits(double credits) {
        return CREDIT_FORMATTER.format(credits);
    }
    
    /**
     * Converts credits from an input string.
     * 
     * @param text the text to be converted to credits
     * @return the converted credits
     * 
     * @throws NumberFormatException if the input does not match
     * 
     * @since 1.00
     */
    public static final double readCredits(String text) {
        try {
            int pos1 = text.indexOf('.');
            if (pos1 >= 0) {
                int pos2 = text.indexOf('.', pos1 + 1);
                if (pos2 < 0) {
                    pos2 = text.indexOf(',');
                    if (pos2 < 0) {
                        text = text.replace(".", ",");
                    }
                }
            }
            return CREDIT_FORMATTER.parse(text).doubleValue();
        } catch (ParseException ex) {
            throw new NumberFormatException(ex.getMessage());
        }
    }

    /**
     * Writes out a list of exercises for the specified user, i.e.
     * except of the user name (which might be taken from a real user)
     * an remaining line in the exercise review table.
     * 
     * @param writer 
     *             the output print writer
     * @param exerciseList 
     *             the list of exercises to be processed
     * @param user 
     *             the user object to be considered
     * @throws IOException
     *             if input/output problems occur 
     * @since 1.00
     */
    private void storeExercises(PrintWriter writer, List<Exercise> 
        exerciseList, User user) throws IOException {
        writer.print(SEPARATOR);
        for (Exercise exercise : exerciseList) {
            Review review =
                getReview(exercise.getName(), user.getUserName());
            if (null != review) {
                writer.print(formatCredits(review.getCredits()));
                writer.print(SEPARATOR);
                writer.print(mask(review.getReview()));
                writer.print(SEPARATOR);
                writer.print(review.isSubmittedToServer());
                writer.print(SEPARATOR);
            } else {
                writer.print(0);
                writer.print(SEPARATOR);
                writer.print(EMPTY_REVIEW);
                writer.print(SEPARATOR);
                writer.print(false);
                writer.print(SEPARATOR);
            }
        }
        writer.println();
    }

    /**
     * Adds the specified exercise to this data structure.
     * 
     * @param exercise
     *            the exercise to be added
     * 
     * @since 1.00
     */
    public void addExercise(Exercise exercise) {
        exercises.put(exercise.getName(), exercise);
    }

    /**
     * Returns the exercise for the specified task.
     * 
     * @param task
     *            the task/exercise the review should be returned for
     * @return the exercise (or <b>null</b> in the case that the review
     *         cannot be found)
     * 
     * @since 1.00
     */
    public Exercise getExercise(String task) {
        return exercises.get(task);
    }

    /**
     * Returns the review for the specified task and user name.
     * 
     * @param task
     *            the task/exercise the review should be returned for
     * @param userName
     *            the user the review should be returned for
     * @return the assigned review (or <b>null</b> in the case that the
     *         review cannot be found)
     * 
     * @since 1.00
     */
    public Review getReview(String task, String userName) {
        Exercise exercise = exercises.get(task);
        if (null == exercise) {
            return null;
        } else {
            return exercise.getReview(userName);
        }
    }
    
    /**
     * Returns all task names of all exercises.
     * 
     * @return the task names of all exercises
     * 
     * @since 1.08
     */
    public List<String> getAllExcerciseTasks() {
        List<String> result = new ArrayList<String>();
        result.addAll(exercises.keySet());
        Collections.sort(result);
        return result;
    }
    
    /**
     * Removes an exercise task.
     * 
     * @param task the name of the task
     * @return <code>true</code> if the task was removed, 
     *         <code>false</code> else
     * 
     * @since 1.08
     */
    public boolean removeExerciseTask(String task) {
        return exercises.remove(task) != null;
    }
    
}

