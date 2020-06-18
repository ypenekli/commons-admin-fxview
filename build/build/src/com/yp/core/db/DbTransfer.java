package com.yp.core.db;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.yp.core.AModel;
import com.yp.core.BaseConstants;
import com.yp.core.db.OnExportListener.PHASE;
import com.yp.core.entity.IResult;
import com.yp.core.tools.DateTime;

public class DbTransfer {

	private ConcurrentHashMap<String, FutureTask<IResult<ITransfer>>> tranfers;
	private ExecutorService executor;
	private int taskCount;

	public DbTransfer(int pTaskCount) {
		super();
		taskCount = pTaskCount;
		tranfers = new ConcurrentHashMap<>(taskCount);
		executor = Executors.newFixedThreadPool(taskCount);
	}

	private static final String FORMATED_EXPORT_MESSAGE1 = "TABLO TOPLAMI/TAMAMLANAN :%s/%s, %s";

	public void transfer(DbConninfo pTarget, ITransfer pTransfer, OnExportListener proceedListener) {
		if (pTransfer != null) {
			FutureTask<IResult<ITransfer>> task = new FutureTask<>(new Callable<IResult<ITransfer>>() {
				@Override
				public IResult<ITransfer> call() throws Exception {
					return new AModel<ITransfer>() {
					}.transferDb(pTarget, pTransfer, proceedListener);
				}
			});

			tranfers.put(pTransfer.getTransferId(), task);
			executor.execute(task);
			while (true) {
				try {
					if (task.isDone()) {
						IResult<ITransfer> res = task.get();
						pTransfer.setMessages(res.getMessage());
						pTransfer.setEndTime(DateTime.dbNow());
						tranfers.remove(pTransfer.getTransferId());
						int remaining = tranfers.size();
						proceedListener.onProceed(PHASE.ENDS_ALL, (double) remaining, taskCount, String
								.format(FORMATED_EXPORT_MESSAGE1, taskCount, taskCount - remaining, res.getMessage()));
						return;
					}
					if (task.isCancelled()) {
						IResult<ITransfer> res = task.get();
						pTransfer.setMessages(res.getMessage());
						tranfers.remove(pTransfer.getTransferId());
						int remaining = tranfers.size();
						proceedListener.onProceed(PHASE.FAILS_ALL, (double) remaining, taskCount, String
								.format(FORMATED_EXPORT_MESSAGE1, taskCount, taskCount - remaining, res.getMessage()));

					}
				} catch (Exception e) {
					pTransfer.setMessages(e.getMessage());
					tranfers.remove(pTransfer.getTransferId());
					int remaining = tranfers.size();
					proceedListener.onProceed(PHASE.FAILS_ALL, (double) remaining, taskCount,
							String.format(FORMATED_EXPORT_MESSAGE1, taskCount, taskCount - remaining, e.getMessage()));
				}
			}
		}
	}

	public void cancelExport(List<ITransfer> list) {
		if (!BaseConstants.isEmpty(list)) {
			list.forEach(e -> tranfers.get(e.getTransferId()).cancel(true));
		}
	}
}
