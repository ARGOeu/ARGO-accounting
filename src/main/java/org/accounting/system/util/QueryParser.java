package org.accounting.system.util;

import com.mongodb.client.model.Filters;
import org.accounting.system.enums.Operand;
import org.accounting.system.enums.Operator;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QueryParser {

    public Bson parseFile(String json, boolean isAlwaysPermission, List<String> objectIds, Class entity) throws ParseException, NoSuchFieldException {
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            String type = (String) jsonObject.get("type");
            Bson filter = null;
            if (type.equals("filter")) {
                filter = parseFilter(jsonObject);

            } else if (type.equals("query")) {
                filter = parseQuery(jsonObject);

            }
            if (!isAlwaysPermission) {
                Bson accessFilter = accessFilter(filter, objectIds, entity);
                return accessFilter;
            } else {
                return filter;
            }
        }
        return null;
    }

    public Bson parseFile(String json) throws ParseException, NoSuchFieldException {
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            String type = (String) jsonObject.get("type");
            Bson filter = null;
            if (type.equals("filter")) {
                filter = parseFilter(jsonObject);

            } else if (type.equals("query")) {
                filter = parseQuery(jsonObject);

            }
            return filter;

        }
        return null;
    }

    private Bson parseFilter(JSONObject filter) throws NoSuchFieldException {
        String operator = (String) filter.get("operator");
        JSONArray criteria = (JSONArray) filter.get("criteria");
        Iterator criteriaIter = criteria.iterator();
        ArrayList<Bson> bsons = new ArrayList<>();
        while (criteriaIter.hasNext()) {
            JSONObject criterio = (JSONObject) criteriaIter.next();
            String type = (String) criterio.get("type");
            if (type.equals("filter")) {
                Bson bson = parseFilter(criterio);
                bsons.add(bson);
            } else if (type.equals("query")) {
                Bson bson = parseQuery(criterio);
                bsons.add(bson);
            }


        }
        if (bsons.size() > 1) {
            Bson queryBson = joinCriteria(operator, bsons);
            return queryBson;
        } else if (bsons.size() == 1) {
            return bsons.get(0);
        } else {
            return null;
        }

    }

    private Bson parseQuery(JSONObject query) {

        String field = (String) query.get("field");
        String operand = (String) query.get("operand");
        if (operand == null) {
            operand = "eq";

        }
        Object val = query.get("values");
        if (val instanceof String && isDate(val)) {
            Instant instant = Instant.parse((String) val);
            Date time = Date.from(instant);
            val = time;

        }
        Bson bson = convertOp(operand, field, val);
        return bson;
    }


    private Bson convertOp(String operand, String field, Object value) {
        Operand op = Operand.getEnumNameForValue(operand);
        switch (op) {

            case EQ:

                return Operand.EQ.execute(field, value);
            case LT:
                return Operand.LT.execute(field, value);
            case LTE:
                return Operand.LTE.execute(field, value);
            case GT:
                return Operand.GT.execute(field, value);
            case GTE:
                return Operand.GTE.execute(field, value);
            case NEQ:
                return Operand.NEQ.execute(field, value);

            default:
                return null;
        }

    }

    private Bson joinCriteria(String operator, ArrayList<Bson> bsons) {
        Operator op = Operator.getEnumNameForValue(operator);
        switch (op) {

            case AND:

                return Operator.AND.execute(bsons);
            case OR:
                return Operator.OR.execute(bsons);
            default:
                return null;
        }

    }

    private boolean isDate(Object val) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

        try {
            dateFormatter.parse((String) val);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;

    }

    public Bson accessFilter(Bson filter, List<String> ids, Class entity) throws NoSuchFieldException {

        if(entity.getDeclaredField("id").getType().getName().equals(ObjectId.class.getName())){
          var oids=  ids.stream().map(id-> new ObjectId(id)).collect(Collectors.toList());
            Bson accessFilter = Filters.in("_id", oids);

           return Filters.and(accessFilter, filter);
        }else{
            Bson accessFilter = Filters.in("_id", ids);

            return Filters.and(accessFilter, filter);

        }
    }
}


