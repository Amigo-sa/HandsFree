package by.citech.handsfree.common;

public interface IProcessing {

    boolean processTask(Task task);
    boolean isTaskCompleted(Task task);

     enum Task {
         Prepare,
         Start,
         Pause,
         Finish,
     }

}
