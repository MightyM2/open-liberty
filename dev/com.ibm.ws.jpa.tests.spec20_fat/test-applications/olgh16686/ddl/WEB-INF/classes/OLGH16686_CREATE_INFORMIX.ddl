CREATE TABLE ${schemaname}.ElemCollEntityOLGH16686 (id INTEGER NOT NULL, PRIMARY KEY (id)) LOCK MODE ROW;
CREATE TABLE ${schemaname}.EntMapDateTemporal (ELEMCOLLENTITYOLGH16686_ID INTEGER, mykey DATE NOT NULL, temporalValue DATE) LOCK MODE ROW;
CREATE INDEX ${schemaname}.I_NTMPPRL_ELEMCOLLENTITYOLGH16686_ID ON ${schemaname}.EntMapDateTemporal (ELEMCOLLENTITYOLGH16686_ID);