package com.dnpa.lab07_determineorientation.Model;

public interface MVC_Model {

    public boolean addToDoItem(String toDoItem, String place) throws Exception;
    public boolean removeToDoItem(long id) throws Exception;
    public boolean modifyToDoItem(long id, String newToDoValuel) throws Exception;
}