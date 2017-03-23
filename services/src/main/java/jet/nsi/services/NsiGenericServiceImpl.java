package jet.nsi.services;

import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import jet.metrics.api.Metrics;
import jet.metrics.api.MetricsDomain;
import jet.nsi.api.NsiError;
import jet.nsi.api.NsiGenericService;
import jet.nsi.api.NsiServiceException;
import jet.nsi.api.data.ConvertUtils;
import jet.nsi.api.data.DictRow;
import jet.nsi.api.data.NsiConfigAttr;
import jet.nsi.api.data.NsiConfigDict;
import jet.nsi.api.data.NsiConfigField;
import jet.nsi.api.data.NsiQuery;
import jet.nsi.api.data.NsiQueryAttr;
import jet.nsi.api.model.BoolExp;
import jet.nsi.api.model.DictRowAttr;
import jet.nsi.api.model.MetaParamValue;
import jet.nsi.api.model.SortExp;
import jet.nsi.api.platform.NsiPlatform;
import jet.nsi.api.sql.SqlDao;
import jet.nsi.api.tx.NsiTransaction;
import jet.nsi.api.tx.NsiTransactionService;
import jet.nsi.common.data.WriteLockNsiDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@MetricsDomain(name = "genericNsiService")
public class NsiGenericServiceImpl implements NsiGenericService {

    private static final Logger log = LoggerFactory
            .getLogger(NsiGenericServiceImpl.class);
    private final Timer dictCountTimer;
    private final Timer dictListTimer;
    private final Timer dictGetTimer;
    private final Timer dictSaveTimer;
    private final Timer dictBatchSaveTimer;
    private final Timer dictDeleteTimer;
    private final Timer dictMergeByExternalAttrs;

    public NsiGenericServiceImpl(Metrics metrics) {
        dictCountTimer = metrics.timer(getClass(), "dictCount");
        dictListTimer = metrics.timer(getClass(), "dictList");
        dictGetTimer = metrics.timer(getClass(), "dictGet");
        dictSaveTimer = metrics.timer(getClass(), "dictSave");
        dictBatchSaveTimer = metrics.timer(getClass(), "dictBatchSave");
        dictDeleteTimer = metrics.timer(getClass(), "dictDelete");
        dictMergeByExternalAttrs = metrics.timer(getClass(), "dictMergeByExternalAttrs");
    }

    private NsiTransactionService transactionService;
    private NsiPlatform nsiPlatform;

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter, SqlDao sqlDao) {
        return dictCount(requestId, query, filter, sqlDao, null, null);
    }

    private long dictCountInternal(NsiTransaction tx, NsiQuery query, BoolExp filter,
                                   SqlDao sqlDao, String sourceQueryName,
                                   Collection<MetaParamValue> sourceQueryParams) {
        checkSourceQuery(query, sourceQueryName);
        return sqlDao.count(tx.getConnection(), query, filter, sourceQueryName, sourceQueryParams);
    }

    @Override
    public long dictCount(String requestId, NsiQuery query, BoolExp filter,
                          SqlDao sqlDao, String sourceQueryName,
                          Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictCountTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                return dictCountInternal(tx, query, filter, sqlDao, sourceQueryName, sourceQueryParams);
            } catch (Exception e) {
                log.error("dictCount [{},{}] -> error", requestId, query.getDict().getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictCount [{},{}] -> error", requestId, query.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
                                  BoolExp filter, List<SortExp> sortList, long offset, int size, SqlDao sqlDao) {
        return dictList(requestId, query, filter, sortList, offset, size, sqlDao, null, null);
    }

    private List<DictRow> dictListInternal(NsiTransaction tx, NsiQuery query,
                                           BoolExp filter, List<SortExp> sortList, long offset, int size,
                                           SqlDao sqlDao, String sourceQueryName,
                                           Collection<MetaParamValue> sourceQueryParams) {
        checkSourceQuery(query, sourceQueryName);

        return sqlDao.list(tx.getConnection(), query, filter, sortList, offset,
                size, sourceQueryName, sourceQueryParams);
    }

    @Override
    public List<DictRow> dictList(String requestId, NsiQuery query,
                                  BoolExp filter, List<SortExp> sortList, long offset, int size,
                                  SqlDao sqlDao, String sourceQueryName,
                                  Collection<MetaParamValue> sourceQueryParams) {
        final Timer.Context t = dictListTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                List<DictRow> data = dictListInternal(tx, query, filter, sortList, offset,
                        size, sqlDao, sourceQueryName, sourceQueryParams);
                log.info("dictList [{},{}] -> ok [{}]", requestId, query.getDict().getName(), data.size());
                return data;
            } catch (Exception e) {
                log.error("dictList [{},{}] -> error", requestId, query.getDict().getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictList [{},{}] -> error", requestId, query.getDict().getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private void checkSourceQuery(NsiQuery query, String sourceQueryName) {
        NsiConfigDict dict = query.getDict();
        if (sourceQueryName != null) {
            Preconditions.checkNotNull(dict.getSourceQuery(sourceQueryName), "dict %s source query %s not exists", dict.getName(), sourceQueryName);
        } else if (dict.getTable() == null) {
            Preconditions.checkNotNull(dict.getSourceQuery(NsiQuery.MAIN_QUERY), "dict %s has not table or default query, source query must set", dict.getName());
        }
    }

    private DictRow dictGetInternal(NsiTransaction tx, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao, BoolExp filter) {
        NsiQuery query = dict.query().addAttrs();
        return sqlDao.get(tx.getConnection(), query, id, filter);
    }

    @Override
    public DictRow dictGet(String requestId, NsiConfigDict dict, DictRowAttr id, SqlDao sqlDao, BoolExp filter) {
        final Timer.Context t = dictGetTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                DictRow data = dictGetInternal(tx, dict, id, sqlDao, filter);
                log.info("dictGet [{},{}] -> ok", requestId, dict.getName());
                return data;
            } catch (Exception e) {
                log.error("dictGet [{},{}] -> error", requestId, dict.getName(), e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictGet [{},{}] -> error", requestId, dict.getName(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    private void validateFields(String requestId, DictRow data) {
        NsiConfigDict dict = data.getDict();
        NsiQuery query = dict.query().addAttrs();
        for (NsiQueryAttr queryAttr : query.getAttrs()) {
            NsiConfigAttr attr = queryAttr.getAttr();

            List<NsiConfigField> fields = attr.getFields();
            String queryAttrName = attr.getName();
            DictRowAttr dataAttr = data.getAttr(queryAttrName);
            if (null == dataAttr)
                continue;

            List<String> dataValues = dataAttr.getValues();

            int i = 0;
            for (NsiConfigField field : fields) {
                String value = dataValues.get(i);
                if (null != value) {
                    switch (field.getType()) {
                        case BOOLEAN:
                            if (!Boolean.TRUE.toString().equals(value)
                                    && !Boolean.FALSE.toString().equals(value))
                                throw NsiExceptionBuilder.on()
                                        .requestId(requestId)
                                        .error(NsiError.CONSTRAINT_VIOLATION)
                                        .message("Атрибут '%s' не прошел валидацию - значение '%s' не является логическим",
                                                field.getName(), value)
                                        .build();
                            break;
                        case NUMBER:
                            // целое
                            if (field.getPrecision() == null || field.getPrecision() == 0) {
                                if (!Pattern.matches(String.format("^[+|-]?\\d{0,%s}$", field.getSize()), value)) {
                                    throw NsiExceptionBuilder.on()
                                            .requestId(requestId)
                                            .error(NsiError.CONSTRAINT_VIOLATION)
                                            .message("Атрибут '%s' не прошел валидацию - значение [%s] не является целым числом с максимальной длинной [%s]",
                                                    field.getName(), value, field.getSize())
                                            .build();
                                }
                            } else {
                                if (!Pattern.matches(String.format("^[+|-]?\\d{0,%s}[.|,]?\\d*$", field.getSize() - field.getPrecision()), value)) {
                                    throw NsiExceptionBuilder.on()
                                            .requestId(requestId)
                                            .error(NsiError.CONSTRAINT_VIOLATION)
                                            .message("Атрибут '%s' не прошел валидацию - значение [%s] не является числом с максимальной длинной целой части [%s]",
                                                    field.getName(), value, field.getSize() - field.getPrecision())
                                            .build();
                                }
                            }
                            break;
                        case VARCHAR:
                        case CHAR:
                            if (value.length() > field.getSize()) {
                                throw NsiExceptionBuilder.on()
                                        .requestId(requestId)
                                        .error(NsiError.CONSTRAINT_VIOLATION)
                                        .message("Атрибут '%s' не прошел валидацию - длина строки [%s] больше максимальной [%s]",
                                                field.getName(), value.length(), field.getSize())
                                        .build();
                            }
                            break;
                        case DATE_TIME:
                            try {
                                ConvertUtils.stringToDateTime(value);
                            } catch (Exception e) {
                                throw NsiExceptionBuilder.on()
                                        .requestId(requestId)
                                        .error(NsiError.CONSTRAINT_VIOLATION)
                                        .message("Атрибут '%s' не прошел валидацию - значение [%s] нельзя преобразовать в дату",
                                                field.getName(), value)
                                        .build();
                            }
                        default:
                            break;
                    }

                    if (null != field.getEnumValues()
                            && !field.getEnumValues().isEmpty()
                            && !field.getEnumValues().keySet().contains(value)) {
                        throw NsiExceptionBuilder.on()
                                .requestId(requestId)
                                .error(NsiError.CONSTRAINT_VIOLATION)
                                .message("Атрибут '%s' не прошел валидацию - значение [%s] не является допустимым значением перечисления",
                                        field.getName(), value)
                                .build();
                    }
                }

                i++;
            }
        }
    }

    private DictRow dictSaveInternal(NsiTransaction tx, DictRow data, SqlDao sqlDao, boolean isInsert, BoolExp filter) {
        NsiConfigDict dict = data.getDict();
        NsiQuery query = dict.query().addAttrs(data);
        DictRow outData;

        if (isInsert) {
            query.addStdAttrs();
            query.addDefaultAttrs();
            //todo check data has idAttr
        }

        try {
            outData = sqlDao.save(tx.getConnection(), query, data, isInsert, filter);
            if (isInsert) {
                log.info("dictSave [{},{}] -> inserted [{}]", tx.getRequestId(),
                        dict.getName(), data.getIdAttr());
            } else {
                log.info("dictSave [{},{}] -> updated [{}]", tx.getRequestId(),
                        dict.getName(), data.getIdAttr());
            }
        } catch (WriteLockNsiDataException e) {
            log.error("dictSave [{},{}] -> error", tx.getRequestId(), data.getDict().getName(), e);
            throw NsiExceptionBuilder.on()
                    .requestId(tx.getRequestId())
                    .error(NsiError.WRITE_LOCK_ERROR)
                    .message(e)
                    .data(e.getData())
                    .build();
        }
        return outData;
    }

    @Override
    public DictRow dictSave(String requestId, DictRow data, SqlDao sqlDao, BoolExp filter) {
        return dictUpdate(requestId, data, sqlDao, false, filter);
    }

    @Override
    public DictRow dictCreate(String requestId, DictRow data, SqlDao sqlDao) {
        return dictUpdate(requestId, data, sqlDao, true, null);
    }

    private DictRow dictUpdate(String requestId, DictRow data, SqlDao sqlDao, boolean isInsert, BoolExp filter){
        final Timer.Context t = dictSaveTimer.time();
        validateFields(requestId, data);
        try (NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                return dictSaveInternal(tx, data, sqlDao, isInsert, filter);
            } catch (NsiServiceException e) {
                tx.rollback();
                throw NsiExceptionBuilder.on().map(e).build();
            } catch (Exception e) {
                log.error("dictSave [{},{}] -> error", requestId, data.getDict().getName(), e);
                tx.rollback();
                throw NsiExceptionBuilder.on()
                        .requestId(requestId)
                        .message(e)
                        .build();
            }
        } catch (NsiServiceException e) {
            throw NsiExceptionBuilder.on().map(e).build();
        } catch (Exception e) {
            log.error("dictSave [{},{}] -> error", requestId, data.getDict().getName(), e);
            throw NsiExceptionBuilder.on()
                    .requestId(requestId)
                    .message(e)
                    .build();
        } finally {
            t.stop();
        }
    }

    public DictRow dictDeleteInternal(NsiTransaction tx, NsiConfigDict dict,
                                      DictRowAttr id, Boolean value, SqlDao sqlDao, BoolExp filter, boolean force) {
        NsiQuery query = dict.query().addAttrs();
        DictRow data = sqlDao.get(tx.getConnection(), query, id, filter);
        DictRow outData = null;
        if (force) {
            sqlDao.delete(tx.getConnection(), query, data, filter);
            outData = data;
        } else {

            data.setDeleteMarkAttr(value);
            outData = sqlDao.update(tx.getConnection(), query, data, filter);
        }
        log.info("dictDelete [{},{},{},{}] -> ok", tx.getRequestId(), dict.getName(), id, value);
        return outData;
    }

    @Override
    public DictRow dictDelete(String requestId, NsiConfigDict dict,
                              DictRowAttr id, Boolean value, SqlDao sqlDao, BoolExp filter, boolean force) {
        final Timer.Context t = dictDeleteTimer.time();
        try (NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                return dictDeleteInternal(tx, dict, id, value, sqlDao, filter, force);
            } catch (Exception e) {
                log.error("dictDelete [{},{},{},{}] -> error", requestId, dict.getName(), id, value, e);
                tx.rollback();
                throw new NsiServiceException(e.getMessage());
            }
        } catch (Exception e) {
            log.error("dictDelete [{},{},{},{}] -> error", requestId, dict.getName(), id, value, e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }
    }

    @Override
    public List<DictRow> dictBatchSave(String requestId, List<DictRow> dataList, SqlDao sqlDao) {
        throw new UnsupportedOperationException("not supported by antifraud");
/*        final Timer.Context t = dictBatchSaveTimer.time();
        for (DictRow data : dataList) {
            validateFields(requestId, data);
        }
        try(NsiTransaction tx = transactionService.createTransaction(requestId, sqlDao.getConnection())) {
            try {
                return dictBatchSaveInternal(tx, dataList, sqlDao);
            } catch (Exception e) {
                log.error("dictBatchSave [{},{}] -> error", requestId, dataList.size(), e);
                tx.rollback();
                if (e instanceof NsiServiceException) {
                    throw e;
                };
                throw new NsiServiceException(e.getMessage());
            }
        } catch (NsiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("dictBatchSave [{},{}] -> error", requestId, dataList.size(), e);
            throw new NsiServiceException(e.getMessage());
        } finally {
            t.stop();
        }*/
    }

    public void setTransactionService(NsiTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public DictRow dictMergeByExternalAttrs(final String requestId, final DictRow data, final SqlDao sqlDao) {
        throw new UnsupportedOperationException("not supported by antifraud");
/*        final Timer.Context t = dictMergeByExternalAttrs.time();
        try {
            return new NsiTransactionTemplate<DictRow>(transactionService, sqlDao.getConnection(), requestId, log) {
                
                @Override
                public DictRow doInTransaction(NsiTransaction tx) {
                    return dictMergeByExternalIdInternal(tx, data, sqlDao);
                }
                
            }.start();

        } catch (SQLException e) {
            throw new NsiServiceException("createTransaction error", e);
        } finally {
            t.stop();
        }*/
    }

    @Override
    public NsiPlatform getNsiPlatform() {
        return nsiPlatform;
    }

    public void setNsiPlatform(NsiPlatform nsiPlatform) {
        this.nsiPlatform = nsiPlatform;
    }
}
