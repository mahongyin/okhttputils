package com.mhy.http.okhttp.utils;

/**
 * Created By Mahongyin
 * Date    2020/8/25 14:29
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public final class ShellUtil {
    /**
     * 执行简单的cmd
     * @param cmd
     * @return
     */
    public static String shellExec(String cmd) {
        Runtime mRuntime = Runtime.getRuntime();
        StringBuilder mRespBuff = new StringBuilder();
        try {
            //Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec(cmd);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));

            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            Log.i("mhyCMD",mRespBuff.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mRespBuff.toString();
    }
    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    /**
     * 不要让任何人实例化此类.
     */
    private ShellUtil() {
        throw new Error("Do not need instantiate!");
    }

    /**
     * 检查是否具有root权限
     *
     * @return
     */
    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }
    /**
     * 执行shell命令，默认返回结果msg
     *
     * @param command command
     * @param isRoot  是否需要以root身份运行
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    /**
     * 执行shell命令，默认返回结果msg
     *
     * @param commands command list
     * @param isRoot   是否需要以root身份运行
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot) {
        return execCommand(
                commands == null ? null : commands.toArray(new String[]{}),
                isRoot, true);
    }

    /**
     * 执行shell命令，默认返回结果msg
     *
     * @param commands command array
     * @param isRoot   是否需要以root身份运行
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /**
     * 执行shell命令
     *
     * @param command         command
     * @param isRoot         是否需要以root身份运行
     * @param isNeedResultMsg 是否需要结果味精
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(String command, boolean isRoot,
                                            boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * 执行shell命令
     *
     * @param commands        command list
     * @param isRoot          是否需要以root身份运行
     * @param isNeedResultMsg 是否需要结果味精
     * @return
     * @see ShellUtil#execCommand(String[], boolean, boolean)
     */
    public static CommandResult execCommand(List<String> commands,
                                            boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(
                commands == null ? null : commands.toArray(new String[]{}),
                isRoot, isNeedResultMsg);
    }

    /**
     * execute shell commands
     *
     * @param commands        command array
     * @param isRoot          是否需要以root身份运行
     * @param isNeedResultMsg 是否需要结果味精
     * @return <ul>
     * <li>如果isNeedResultMsg为false, {@link CommandResult#successMsg}
     * 为null并且{@link CommandResult＃errorMsg}为null.</li>
     * <li>如果{@link CommandResult＃result}为-1，则可能存在异常.</li>
     * </ul>
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot,
                                            boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(
                    isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(
                        process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null
                : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    /**
     * 命令结果
     * <ul>
     * <li>{@link CommandResult#result} 表示命令结果，0表示正常,
     * else表示错误，与在Linux shell中执行相同</li>
     * <li>{@link CommandResult#successMsg} 表示命令成功消息
     * 结果</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     */
    public static class CommandResult {

        /**
         * result of command *
         */
        public int result;
        /**
         * success message of command result *
         */
        public String successMsg;
        /**
         * error message of command result *
         */
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}

