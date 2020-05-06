package net.jmecn.zkxui.gui.task;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import net.jmecn.zkxui.gui.dialog.CenterUtils;

/**
 * 
 * @title ProcessDialog
 * @author yanmaoyuan
 * @date 2020年5月6日
 * @version 1.0
 */
public class ProgressDialog<T> extends JDialog {

    private static final long serialVersionUID = 1L;

    private ExecutorService executorService = null;

    private ProgressTask<T> task = null;

    private Future<T> future = null;

    private T result = null;

    private Timer timer = null;

    private JProgressBar progressBar;

    /**
     * @param args
     */
    public static void main(String[] args) {
        ProgressTask<String> task = new ProgressTask<String>() {
            @Override
            public String call() {
                try {
                    Thread.sleep(3000L);
                    return "OK";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        ProgressDialog<String> dialog = new ProgressDialog<String>("Process", task);
        dialog.setVisible(true);

        String result = dialog.getResult();
        System.out.println(result);
    }

    public ProgressDialog(String title, ProgressTask<T> task) {
        this.task = task;

        this.setTitle(title);
        this.setSize(320, 120);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(true);
        CenterUtils.center(this);

        // set up layout
        setContentPane(getMainPanel());

        // submit the task;
        executorService = Executors.newFixedThreadPool(1);
        this.future = executorService.submit(task);

        // Use Swing Timer instead of ExecutorService, so the watcher is running on GUI Thread,
        // It can dispose the dialog safely.
        timer = new Timer(200, watcher);
        timer.setRepeats(true);
        timer.start();

        // Terminate the threads when closing dialog.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evn) {
                terminate();
            }
        });
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setFocusable(false);
        if (!task.isIndeterminate()) {
            progressBar.setMaximum(task.maximun());
            progressBar.setValue(0);
        }
        progressBar.setIndeterminate(task.isIndeterminate());// 采用确定的进度条样式
        progressBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = new JButton("cancel");
        south.add(cancel);
        cancel.addActionListener((e) -> {
            terminate();
            dispose();
        });

        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private ActionListener watcher = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (future.isDone()) {
                // Get the result
                try {
                    result = future.get(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }

                // kill the dialog
                terminate();
                dispose();
            } else if (future.isCancelled()) {

                // kill the dialog
                terminate();
                dispose();
            } else {
                // Task is not done and not canceled.
                // Refresh the progress bar if needed.
                refreshProgress();
            }
        }
    };

    private void refreshProgress() {
        if (task.isIndeterminate()) {
            return;
        }

        progressBar.setValue(task.value());
        progressBar.setMaximum(task.maximun());
    }

    private void terminate() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }

        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public T getResult() {
        return result;
    }
}
