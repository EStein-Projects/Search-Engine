package edu.yu.cs.com1320.searchengine.undo;

import java.util.*;

public class CommandSet<Target> extends AbstractSet<Command<Target>> implements Undoable {

    private HashSet<Command<Target>> commands;

    public CommandSet(){
        this.commands = new HashSet<Command<Target>>();
    }

    /**
     * does this CommandSet include a command whose target is c?
     * @param c
     * @return
     */
    public boolean containsTarget(Target c) {
        return this.commands.contains(new Command<>(c,null));
    }

    /**
     * Add a command to this command set.
     * A single Target can only have ONE command in the set.
     * @param command
     * @throws IllegalArgumentException if this set already contains a command for this Target
     */
    public void addCommand(Command<Target> command){
        if(containsTarget(command.getTarget())){
            throw new IllegalArgumentException("this CommandSet already has a command for " + command.getTarget().toString());
        }
        this.commands.add(command);
    }

    /**
     *
     * @param c the target to undo
     */
    public boolean undo(Target c){
        if(containsTarget(c)){
            Command<Target> todo = null;
            for(Command<Target> cmd : this.commands){
                if(cmd.getTarget().equals(c)){
                    todo = cmd;
                    break;
                }
            }
            if(todo != null){
                this.commands.remove(todo);
                return todo.undo();

            }
        }
        return false;
    }

    /**
     *
     * @return true if ALL the commands were undone, false if not
     */
    @Override
    public boolean undo() {
        int size = this.commands.size();
        return this.undoAll().size() == size;
    }
    /**
     * undo all the commands in this command set.
     * all undone commands are removed from the command set.
     * @return a set of the undone commands
     */
    public Set<Command<Target>> undoAll(){
        HashSet<Command<Target>> undone = new HashSet<>(this.commands.size());
        Object[] allCommands = this.commands.toArray();
        for(Object cmdObj : allCommands){
            Command<Target> cmd = (Command<Target>)cmdObj;
            if(cmd.undo()){
                undone.add(cmd);
                this.commands.remove(cmd);
            }
        }
        return undone;
    }

    @Override
    public Iterator<Command<Target>> iterator() {
        return this.commands.iterator();
    }

    @Override
    public int size() {
        return this.commands.size();
    }
}