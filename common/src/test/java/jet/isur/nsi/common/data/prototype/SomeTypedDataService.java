package jet.isur.nsi.common.data.prototype;

import java.util.ArrayList;
import java.util.List;

import jet.isur.nsi.api.data.builder.DictRowAttrBuilder;
import jet.isur.nsi.api.model.BoolExp;
import jet.isur.nsi.api.model.DictRow;
import jet.isur.nsi.api.model.SortExp;
import jet.isur.nsi.common.data.DictDataManager;

class SomeTypedDataService {
    private static final String SOME_DICT = "someDict";
    DictDataManager dataService;
/*
    public SomeTyped get(long id) throws Exception {
        DictRow dictRow = dataService.get(SOME_DICT,DictRowAttrBuilder.from("id", id));

        SomeTyped result = new SomeTyped();
        reader = new DictRowReader(query);
        converter.convertTo(dictRow, result);
        return result;
    }

    public SomeTyped save(SomeTyped data) throws Exception {

        DictRow dictRow = new DictRow();
        DictRowSomeTypedConverter converter = new DictRowSomeTypedConverter();
        converter.convertFrom(data, dictRow);

        converter.convertTo(dataService.save(SOME_DICT,dictRow,true), data);
        return data;
    }

    public List<SomeTyped> listSomeTyped(BoolExp filter, List<SortExp> sortList, int offset, int size) throws Exception {
        List<DictRow> dictRowList = dataService.list(SOME_DICT,filter,sortList,offset,size);
        List<SomeTyped> result = new ArrayList<>(dictRowList.size());
        DictRowSomeTypedConverter converter = new DictRowSomeTypedConverter();
        for (DictRow dictRow : dictRowList) {
            SomeTyped data = new SomeTyped();
            converter.convertTo(dictRow, data);
            result.add(data);
        }
        return result;
    }
    */
}
