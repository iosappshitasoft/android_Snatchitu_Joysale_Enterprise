package com.ni.addons.smartreply;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.os.HandlerCompat;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.mlkit.nl.smartreply.SmartReply;
import com.google.mlkit.nl.smartreply.SmartReplyGenerator;
import com.google.mlkit.nl.smartreply.SmartReplySuggestion;
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult;
import com.google.mlkit.nl.smartreply.TextMessage;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerateSmartReplyWorker extends ListenableWorker {
    private static final String TAG = GenerateSmartReplyWorker.class.getSimpleName();
    private Context mContext;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private final SmartReplyGenerator smartReply = SmartReply.getClient();

    public GenerateSmartReplyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        final String lastMessage = getInputData().getString(Constants.TAG_MESSAGE);
        return CallbackToFutureAdapter.getFuture(completer -> {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    List<TextMessage> chatHistory = new ArrayList<>();
                    chatHistory.add(TextMessage.createForRemoteUser(lastMessage, System.currentTimeMillis(), GetSet.getUserId()));
                    smartReply.suggestReplies(chatHistory)
                            .addOnSuccessListener(smartReplySuggestionResult -> {
                                if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                                    // The conversation's language's hashMap isn't supported, so
                                    // the result doesn't contain any suggestions.
                                    Log.e(TAG, "generateSmartReply: " + SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE);
                                    setError(completer);
                                } else if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                                    // Task completed successfullyactivity
                                    // ...
                                    List<SmartReplySuggestion> suggestion = smartReplySuggestionResult.getSuggestions();
                                    if (suggestion.size() > 0) {
                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                String suggestionString = new Gson().toJson(suggestion);
                                                completer.set(Result.success(createOutputData(suggestionString)));
                                            }
                                        });
                                    } else {
                                       setError(completer);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "generateSmartReply: " + e.getMessage());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setError(completer);
                                }
                            });
                        }
                    });
                }
            });
            return completer;
        });
    }

    private Data createOutputData(String languageJson) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_DATA, languageJson);
        return builder.build();
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                completer.set(Result.failure(Data.EMPTY));
            }
        });
    }
}
