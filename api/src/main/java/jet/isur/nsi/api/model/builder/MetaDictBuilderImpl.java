package jet.isur.nsi.api.model.builder;

import java.util.ArrayList;

import jet.isur.nsi.api.model.MetaAttr;
import jet.isur.nsi.api.model.MetaAttrType;
import jet.isur.nsi.api.model.MetaDict;
import jet.isur.nsi.api.model.MetaField;
import jet.isur.nsi.api.model.MetaFieldType;

public class MetaDictBuilderImpl implements MetaDictBuilder {

    private class MetaFieldBuilderImpl implements MetaFieldBuilder {
        private final MetaDictBuilderImpl owner;
        private MetaField prototype;

        public MetaFieldBuilderImpl(MetaDictBuilderImpl owner) {
            this.owner = owner;
        }

        private MetaField getPrototype() {
            if(prototype == null) {
                prototype = new MetaField();
            }
            return prototype;
        }

        @Override
        public MetaFieldBuilder name(String value) {
            getPrototype().setName(value);
            return this;
        }

        @Override
        public MetaFieldBuilder type(MetaFieldType value) {
            getPrototype().setType(value);
            return this;
        }

        @Override
        public MetaFieldBuilder size(Integer value) {
            getPrototype().setSize(value);
            return this;
        }

        @Override
        public MetaFieldBuilder precision(Integer value) {
            getPrototype().setPrecision(value);
            return this;
        }

        @Override
        public MetaDictBuilder add() {
            owner.getPrototype().getFields().add(getPrototype());
            prototype = null;
            return owner;
        }

    }

    private class MetaAttrBuilderImpl implements MetaAttrBuilder {
        protected final MetaDictBuilderImpl owner;
        private MetaAttr prototype;

        public MetaAttrBuilderImpl(MetaDictBuilderImpl owner) {
            this.owner = owner;
        }

        protected MetaAttr getPrototype() {
            if(prototype == null) {
                prototype = new MetaAttr();
                prototype.setFields(new ArrayList<String>());
            }
            return prototype;
        }

        @Override
        public MetaDictBuilder add() {
            owner.getPrototype().getAttrs().add(getPrototype());
            prototype = null;
            return owner;
        }

        @Override
        public MetaAttrBuilder type(MetaAttrType value) {
            getPrototype().setType(value);
            return this;
        }

        @Override
        public MetaAttrBuilder valueType(String value) {
            getPrototype().setValueType(value);
            return this;
        }

        @Override
        public MetaAttrBuilder name(String value) {
            getPrototype().setName(value);
            return this;
        }

        @Override
        public MetaAttrBuilder caption(String value) {
            getPrototype().setCaption(value);
            return this;
        }

        @Override
        public MetaAttrBuilder addField(String value) {
            getPrototype().getFields().add(value);
            return this;
        }

        @Override
        public MetaAttrBuilder refDict(String value) {
            getPrototype().setRefDict(value);
            return this;
        }

        @Override
        public MetaAttrBuilder hidden(Boolean value) {
            getPrototype().setHidden(value);
            return this;
        }

		@Override
		public MetaAttrBuilder createOnly(Boolean value) {
			getPrototype().setCreateOnly(value);
			return this;
		}

	}

    private MetaDict prototype;


    public MetaDict getPrototype() {
        if(prototype == null) {
            prototype = new MetaDict();
            prototype.setFields(new ArrayList<MetaField>());
            prototype.setAttrs(new ArrayList<MetaAttr>());
            prototype.setCaptionAttrs(new ArrayList<String>());
            prototype.setRefObjectAttrs(new ArrayList<String>());
            prototype.setTableObjectAttrs(new ArrayList<String>());
        }
        return prototype;
    }

    @Override
    public MetaDict build() {
        MetaDict result = getPrototype();
        prototype = null;
        return result;
    }

    @Override
    public MetaDictBuilder table(String value) {
        getPrototype().setTable(value);
        return this;
    }

    @Override
    public MetaFieldBuilder field() {
        return new MetaFieldBuilderImpl(this);
    }

    @Override
    public MetaAttrBuilder attr() {
        return new MetaAttrBuilderImpl(this);
    }

    @Override
    public MetaDictBuilder idAttr(String name) {
        getPrototype().setIdAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder parentAttr(String name) {
        getPrototype().setParentAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder isGroupAttr(String name) {
        getPrototype().setIsGroupAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder ownerAttr(String name) {
        getPrototype().setOwnerAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder deleteMarkAttr(String name) {
        getPrototype().setDeleteMarkAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder lastChangeAttr(String name) {
        getPrototype().setLastChangeAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder lastUserAttr(String name) {
        getPrototype().setLastUserAttr(name);
        return this;
    }

    @Override
    public MetaDictBuilder addCaptionAttr(String value) {
        getPrototype().getCaptionAttrs().add(value);
        return this;
    }

    @Override
    public MetaDictBuilder addRefObjectAttr(String value) {
        getPrototype().getRefObjectAttrs().add(value);
        return this;
    }

    @Override
    public MetaDictBuilder addTableObjectAttr(String value) {
        getPrototype().getTableObjectAttrs().add(value);
        return this;
    }

    @Override
    public MetaDictBuilder name(String value) {
        getPrototype().setName(value);
        return this;
    }

    @Override
    public MetaDictBuilder caption(String value) {
        getPrototype().setCaption(value);
        return this;
    }
}
