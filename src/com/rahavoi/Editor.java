package com.rahavoi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Editor {
    private long cursor;
    private String text;
    private Stack<EditorState> states = new Stack<>();

    public Editor(String text, String input) {
        this.text = text;
        this.init(input);
        states.push(new EditorState(0, text));
    }

    private void init(String input){
        List<Command> commands = this.parseCommands(input);
        commands.forEach(Command::run);
    }

    private List<Command> parseCommands(String input){
        List<Command> commands  = new ArrayList<>();
        char[] chars = input.toCharArray();
        StringBuilder times = new StringBuilder();

        for(int i = 0; i < chars.length; i++){
            char c = chars[i];
            if(Character.isDigit(c)){
                times.append(c);
            } else {
                Command command = null;
                int n = times.length() == 0 ? 1 : toInt(times.toString()) < 0 ?
                        Integer.MAX_VALUE: toInt(times.toString());

                switch (c){
                    case 'h':
                        command = new MoveLeft(n);
                        break;
                    case 'l':
                        command = new MoveRight(n);
                        break;
                    case 'r':
                        command = new Replace(n, chars[i + 1]);
                        i++;
                        break;
                    case 'f':
                        command = new MoveToNext(chars[i + 1]);
                        i++;
                        break;
                    case 'x':
                        command = new Delete(n);
                        break;
                    case 'u':
                        command = new Undo(n);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported command: " + c);
                }
                commands.add(command);
                times = new StringBuilder();
            }
        }
        return commands;
    }

    private int toInt(String input){
        return new BigInteger(input.toString()).intValue();
    }

    //TODO: Creating a new state for each command is not very efficient when there are many states
    // and/or text is big. Alternatively we could implement undo logic for each command, but I am lazy.
    private class Undo extends Command {
        Undo(int times){
            super(times);
        }

        public void apply(){
            EditorState state = new EditorState(cursor, text);

            for(int i = 0; i < times; i++){
                if(states.isEmpty()){
                    break;
                }
                state = states.pop();
            }

            text = state.text;
            cursor = state.cursor;
        }
    }

    private class Delete extends Command {
        Delete(int times){
            super(times);
        }

        public void apply(){
            if(cursor + times >= text.length()){
                text = text.substring(0, (int) cursor);
            } else {
                text = text.substring(0, (int) cursor) + text.substring((int) (cursor + times));
            }
        }
    }

    private class MoveToNext extends Command {
        private final char moveTo;

        MoveToNext(char c){
            super();
            this.moveTo = c;
        }

        @Override
        void apply(){
            for(int i = (int) cursor; i < text.length(); i++){
                if(text.charAt(i) == moveTo){
                    cursor = i;
                    break;
                }
            }
        }
    }

    private class Replace extends Command {
        private final char characterToReplace;
        Replace(int times, char c){
            super(times);
            this.characterToReplace = c;
        }

        @Override
        void apply(){
            if(this.times == 1){
                replace();
            } else {
                for(int i = 0; i < times; i++){
                    replace();
                    cursor++;
                    if(cursor >= text.length()){
                        cursor--;
                        break;
                    }
                }
            }
        }

        private void replace(){
            text = text.substring(0, (int) cursor) + characterToReplace + text.substring((int) cursor + 1);
        }
    }

    private class MoveRight extends Command {
        MoveRight(int times){ super(times);}
        @Override
        void apply(){
            cursor = Math.min(text.length() - 1, cursor + this.times);
        }
    }

    private class MoveLeft extends Command {
        MoveLeft(int times){ super(times);}
        @Override
        void apply(){
            cursor = Math.max(0, cursor - this.times);
        }
    }

    private class EditorState {
        long cursor;
        String text;

        public EditorState(long cursor, String text) {
            this.cursor = cursor;
            this.text = text;
        }
    }

    private abstract class Command {
        long times;
        void run(){
            if(!(this instanceof Undo)){
                states.push(new EditorState(cursor, text));
            }

            this.apply();

        }
        abstract void apply();

        public Command(){};
        public Command(int times) {
            this.times = times;
        }
    }

    public void print(){
        System.out.println(text);
        System.out.println("Cursor: " + cursor);
    }
}
