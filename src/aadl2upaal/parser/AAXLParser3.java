package aadl2upaal.parser;

import aadl2upaal.aadl.*;
import aadl2upaal.upaal.Location;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by haven on 16/6/14.
 */
public class AAXLParser3 {

    public File aaxlFile;
    private AADLModel amodel;
    private XPathFactory xpfac;
    private Document doc;

    public AAXLParser3(File aaxlFile) {
        this.aaxlFile = aaxlFile;
        xpfac = XPathFactory.instance();
    }

    //That's EZ
    public AADLModel createAADLModel() throws Exception {

        SAXBuilder builder = new SAXBuilder(false);
        doc = builder.build(aaxlFile);

        Namespace aadl2 = Namespace.getNamespace("aadl2", "http://aadl.info/AADL/2.0");
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        Element sys = doc.getRootElement();
        Attribute name = sys.getAttribute("name");
        amodel = new AADLModel(name.getValue());

        List<Element> compoents = sys.getChild("ownedPublicSection").getChildren("ownedClassifier");
        for (Element ele : compoents) {
            Attribute type = ele.getAttribute("type", xsi);
            Attribute comp_name = ele.getAttribute("name");

            // 遍历每个component
            if (type.getValue().contains("System")) {
                if (type.getValue().contains("Implementation")) {

                } else {

                }

            } else if (type.getValue().contains("Abstract")) {
                if (type.getValue().contains("Implementation")) {
                    //把impl 加入到 component 中
                    String comp_name_string = comp_name.getValue().substring(0, comp_name.getValue().indexOf("."));
                    ACompoentImpl ab_impl = new ACompoentImpl(comp_name_string);
                    for (ACompoent comp : amodel.comps) {
                        if (comp.getName().equals(comp_name_string)) {
                            comp.setCompoentImpl(ab_impl);
                        }
                    }

                    //遍历所有annex
                    for (Element annex : ele.getChildren("ownedAnnexSubclause")) {
                        Element parsedAnnexSubclause = annex.getChild("parsedAnnexSubclause");
                        String annex_name = parsedAnnexSubclause.getAttributeValue("name");
                        process_annex(annex_name, parsedAnnexSubclause, ab_impl);
                    }


                } else {
                    ACompoent abstract_comp = new ACompoent(comp_name.getValue());
                    ACompoentDeclare ab_declare = new ACompoentDeclare(comp_name.getValue());

                    process_ports(ele, ab_declare);

                    amodel.comps.add(abstract_comp);
                    abstract_comp.setCompoentDeclare(ab_declare);
                }
            } else if (type.getValue().contains("Process")) {
                if (type.getValue().contains("Implementation")) {

                } else {

                }
            } else if (type.getValue().contains("Thread")) {

                if (type.getValue().contains("Implementation")) {
                    //把impl 加入到 component 中
                    String comp_name_string = comp_name.getValue().substring(0, comp_name.getValue().indexOf("."));
                    ACompoentImpl th_impl = new ACompoentImpl(comp_name_string);
                    for (ACompoent comp : amodel.comps) {
                        if (comp.getName().equals(comp_name_string)) {
                            comp.setCompoentImpl(th_impl);
                        }
                    }

                    //遍历所有annex
                    for (Element annex : ele.getChildren("ownedAnnexSubclause")) {
                        Element parsedAnnexSubclause = annex.getChild("parsedAnnexSubclause");
                        String annex_name = parsedAnnexSubclause.getAttributeValue("name");
                        process_annex(annex_name, parsedAnnexSubclause, th_impl);
                    }
                } else {
                    ACompoent thread_comp = new ACompoent(comp_name.getValue());
                    ACompoentDeclare th_declare = new ACompoentDeclare(comp_name.getValue());
//                    process_ports(ele, de_declare);

                    amodel.comps.add(thread_comp);
                    thread_comp.setCompoentDeclare(th_declare);
                }
            } else if (type.getValue().contains("Device")) {

                if (type.getValue().contains("Implementation")) {
                    //把impl 加入到 component 中
                    String comp_name_string = comp_name.getValue().substring(0, comp_name.getValue().indexOf("."));
                    ACompoentImpl de_impl = new ACompoentImpl(comp_name_string);
                    for (ACompoent comp : amodel.comps) {
                        if (comp.getName().equals(comp_name_string)) {
                            comp.setCompoentImpl(de_impl);
                        }
                    }

                    //遍历所有annex
                    for (Element annex : ele.getChildren("ownedAnnexSubclause")) {
                        Element parsedAnnexSubclause = annex.getChild("parsedAnnexSubclause");
                        String annex_name = parsedAnnexSubclause.getAttributeValue("name");
                        process_annex(annex_name, parsedAnnexSubclause, de_impl);
                    }
                } else {
                    ACompoent device_comp = new ACompoent(comp_name.getValue());
                    ACompoentDeclare de_declare = new ACompoentDeclare(comp_name.getValue());

                    process_ports(ele, de_declare);

                    amodel.comps.add(device_comp);
                    device_comp.setCompoentDeclare(de_declare);

                }
            } else {
                throw new RuntimeException("not support compoent");
            }


            //System.out.println("comp");
        }

        System.out.println("parsed aadl xml to model by jdom");

//      Connection 的作用是去重!!
        // 所以controller 就不用管它的port了!!
//        getConnections(model);
//
        //TODO 最好能有一个字符串 到 对象的映射表  , 比如 tv 有它对应的对象, 这样以后引用tv的时候 就不用创建新的了
        // 这也可以每次都遍历已有的对象, 但可能会有命名冲突的问题
        // 还有一个问题是 可能引用还没有创建的对象
        return amodel;
    }

    private void process_ports(Element ele, ACompoentDeclare ab_declare) {
        //get all port
        for (Element port : ele.getChildren("ownedDataPort")) {
            process_date_port(ab_declare, port);
        }
        for (Element port : ele.getChildren("ownedEventDataPort")) {
            process_date_port(ab_declare, port);
        }
        for (Element port : ele.getChildren("ownedEventPort")) {
            process_event_port(ab_declare, port);
        }
    }

    private void process_annex(String annex_name, Element parsedAnnexSubclause, ACompoentImpl impl) {
        if (annex_name.equals("hybrid")) {
            HybirdAnnex hybirdAnnex = new HybirdAnnex("");
            //source text pre-process
            String sourceText= "";
            sourceText = parsedAnnexSubclause.getAttributeValue("sourceText","");
            sourceText = sourceText.replace("\t", "");
            sourceText = sourceText.replace("\r", "");
            sourceText = sourceText.replaceAll("--.*?\n", "\n");

            //variable
            Pattern variablesCompile = Pattern.compile("variables([\\s\\S]*)?(constants|behavior)");
            Matcher variablesMatcher = variablesCompile.matcher(sourceText);
            if (variablesMatcher.find()) {
                String variables = variablesMatcher.group(1);
                String[] split = variables.split("\n");
                Pattern varCompile = Pattern.compile("(\\S+)\\s*?:\\s*?(\\S+)");
                for (String variable : split) {
                    Matcher m = varCompile.matcher(variable);
                    if (m.find()) {
                        String name = m.group(1);
                        String type = m.group(2);
                        hybirdAnnex.getVariables().add(new AVar(name,type));
                    }
                }
            }
            //constants
//            Element con = parsedAnnexSubclause.getChild("con");
//            for(Element cn : con.getChildren("const")){
//                String c_name = cn.getChild("constant").getAttributeValue("name");
//                HConstant c = new HConstant(c_name, "");
//                //c.setInitVal(0.7);
//                hybirdAnnex.getConstants().add(c);
//
//            }

            //behavior
            Pattern behaviorCompile = Pattern.compile("behavior(.*)",Pattern.COMMENTS|Pattern.DOTALL);
            Matcher behaviorMatcher = behaviorCompile.matcher(sourceText);
            Pattern statementCompile = Pattern.compile("(\\S*)\\s*::=(.*)");
            Pattern communicateCompile = Pattern.compile("^(.*)\\[\\[\\>(.*)\\]\\]\\>\\s*(\\S+)");
            Pattern continuousCompile = Pattern.compile("'\\s*DT\\s+(\\d+)\\s+(\\S+)\\s*=\\s*(\\S.*?)\\s*'");
            Pattern channelCompile = Pattern.compile("(\\S+)(\\?|\\!)(\\(\\S+\\))?");
            Pattern assignmentCompile = Pattern.compile("^\\s*(\\S+)\\s*:=\\s*(.*?)\\s*$");
            Pattern intCompile = Pattern.compile("(-?\\d+)");
            Pattern doubleCompile = Pattern.compile("(-?\\d*\\.\\d+)");
            Pattern repetitionCompile = Pattern.compile("REPEAT\\s*\\(\\s*(\\S+)\\s*\\)");
            if (behaviorMatcher.find()) {
                String behavior = behaviorMatcher.group(1);
                String[] split = behavior.split("\n");
                ArrayList<String> statements = new ArrayList<String>();
                for(String beh: split){
                    if(beh.contains("::=")){
                        statements.add(statements.size(),beh);
                    }else if(!statements.isEmpty()){
                        String origin = statements.remove(statements.size()-1);
                        statements.add(statements.size(),origin.concat(beh));
                    }
                }
                for(String statement:statements){
                    Matcher statementMatcher = statementCompile.matcher(statement);
                    if(statementMatcher.find()){
                        String left = statementMatcher.group(1);
                        String right = statementMatcher.group(2);
                        HybirdProcess hp = new HybirdProcess();
                        hp.setName(left);
                        //skip
                        if(right.contains("skip")) {
                            hp.setSkip(true);
                            hybirdAnnex.getBehavior().add(hp);
                        }else{
                            //interrupt
                            Matcher communicateMatcher = communicateCompile.matcher(right);
                            if(communicateMatcher.find()){
                                String continuous = communicateMatcher.group(1);
                                String channels = communicateMatcher.group(2);
                                String nextprocess = communicateMatcher.group(3);
                                //continuous
                                String[] conts = continuous.split("&");
                                for(String cont:conts){
                                    Matcher continuousMatcher = continuousCompile.matcher(cont);
                                    if(continuousMatcher.find()) {
                                        String order = continuousMatcher.group(1);
                                        String variable = continuousMatcher.group(2);
                                        String value = continuousMatcher.group(3);
                                        HContinuous hContinuous = new HContinuous();
                                        hContinuous.setRank(Integer.parseInt(order));
                                        hContinuous.setLeft(new AVar(variable, ""));
                                        hContinuous.setRight(new AVar(value, ""));
                                        hp.getEvolutions().add(hContinuous);
                                    }
                                }
                                //interrupt
                                HInterrupt hInterrupt = new HInterrupt();
                                String[] comms = channels.split(",");
                                for(String comm:comms){
                                    Matcher channelMatcher = channelCompile.matcher(comm);
                                    if(channelMatcher.find()){
                                        String channel = channelMatcher.group(1);
                                        String direction = channelMatcher.group(2);
                                        String variable = channelMatcher.group(3);
                                        HCommunication hc1 = new HCommunication();
                                        if(direction.equals("!")){
                                            hc1.setDirection(APort.out);
                                        }else{
                                            hc1.setDirection(APort.in);
                                        }
                                        hc1.setP(new ADataPort(channel));
                                        if(variable!=null) {
                                            hc1.setVar(new AVar(variable, ""));
                                        }
                                        hInterrupt.getComm().add(hc1);
                                    }
                                }
                                hp.setInterrupt(hInterrupt);
                            }else{
                                String[] subprocesses = right.split("&");
                                for(String subprocess:subprocesses){
                                    //assignment
                                    Matcher assignmentMatcher = assignmentCompile.matcher(subprocess);
                                    if(assignmentMatcher.find()){
                                        String var = assignmentMatcher.group(1);
                                        String value = assignmentMatcher.group(2);
                                        Hassignment hassignment = new Hassignment();
                                        hassignment.setVar(new AVar(var,""));
                                        if(doubleCompile.matcher(value).matches()){
                                            hassignment.setVal(Double.parseDouble(value));
                                        }else if(intCompile.matcher(value).matches()){
                                            hassignment.setVal(Integer.parseInt(value));
                                        }else{
                                            hassignment.right = value;
                                        }
                                        hp.getAssignments().add(hassignment);
                                    }
                                    Matcher repetitionMatcher = repetitionCompile.matcher(subprocess);
                                    if(repetitionMatcher.find()){
                                        String repeat = repetitionMatcher.group(1);
                                        for(HybirdProcess p : hybirdAnnex.getBehavior()){
                                            if(p.getName().equals(repeat)){
                                                p.isRepete =true;
                                                hp.subProcess = p;
                                                break;
                                            }
                                        }
                                        hp.isIinitial = true;
                                    }
                                }
                            }
                            hybirdAnnex.getBehavior().add(hp);
                        }
                    }
                }
            }
            impl.getAnnexs().add(hybirdAnnex);
        } else if (annex_name.equals("BLESS")) {
            BLESSAnnex ba = new BLESSAnnex("");

            // invariant
            Element invariant = parsedAnnexSubclause.getChild("invariant");
            if (invariant != null) {
                ;
            }

            //var
            Element var = parsedAnnexSubclause.getChild("variables");
            if (var != null) {
                for (Element bv : var.getChildren("bv")) {
                    String v_name = bv.getChild("variable_names").getAttributeValue("variable");
                    Element type = bv.getChild("type");
                    String v_type = "";
//                    Element nt = bv.getChild("nt");
//                    if(nt != null){
//                        v_type = nt.getAttributeValue("assertion_type");
//                    }else{
//                        v_type = type.getAttributeValue("data_component_reference");
//                        Pattern typePattern = Pattern.compile("#(\\w+)\\.(\\w+)");
//                        Matcher typeMatcher = typePattern.matcher(v_type);
//                        if(typeMatcher.find()){
//                            String ns = typeMatcher.group(1);
//                            String t = typeMatcher.group(2);
//                            v_type = ns + "::" + t;
//                        }
//                    }
                    v_type = "double";
                    BVar v = new BVar(v_name, v_type);

                    Element expression = bv.getChild("expression");
                    if (expression != null) {
                        Element init_val = expression.getChild("se").getChild("value");
                        String nul = init_val.getAttributeValue("nul","0");
                        if(nul=="true"){
                            v.setInitVal(0);
                        }
                        Element val = init_val.getChild("const");
                        if(val!=null){
                            v.setInitVal(Integer.valueOf(val.getChild("number").getAttributeValue("integer","0")));
                        }
                    }
                    ba.getVariables().add(v);
                }
            }
            //states
            List<Element> states = parsedAnnexSubclause.getChildren("states");
            for (Element state : states) {
                Location loc = new Location(state.getAttributeValue("name"), null);
                String initial = state.getAttributeValue("initial", "null");
                String complete = state.getAttributeValue("complete", "null");
//                String finalstate = state.getAttributeValue("final", "null");
                if (initial.equals("true")) {
                    loc.isInitial = true;
                    loc.isCommitted = true;
                }
//                if (complete.equals("true")) {
//                    loc.isCommitted = true;
//                }
//                if (finalstate.equals("true")) {
//                    loc.is = true;
//                }
                ba.getLocs().add(loc);
            }
            //transitions
            Element transitions = parsedAnnexSubclause.getChild("transitions");
            if (transitions != null) {
                for (Element bt : transitions.getChildren("bt")) {
                    String sources = bt.getAttributeValue("sources");
                    Element source_ele = (Element) xpfac.compile(Utils.convert2xpath(sources), Filters.element()).evaluateFirst(doc.getRootElement());
                    Location src_loc = Utils.find_state_by_name(source_ele.getAttributeValue("name", ""), ba.getLocs());

                    String destination = bt.getAttributeValue("destination");
                    Element dst_ele = (Element) xpfac.compile(Utils.convert2xpath(destination), Filters.element()).evaluateFirst(doc.getRootElement());
                    Location dst_loc = Utils.find_state_by_name(dst_ele.getAttributeValue("name", ""), ba.getLocs());

                    String bt_name = bt.getChild("transition_label").getAttributeValue("id");
                    BTransition bt_aadl = new BTransition();
                    bt_aadl.setSrc(src_loc);
                    bt_aadl.setDst(dst_loc);
                    bt_aadl.setName(bt_name);

                    //guard
                    Element execute = bt.getChild("execute");
                    if (execute != null) {
                        String source_annex = parsedAnnexSubclause.getParentElement().getAttributeValue("sourceText");
                        Pattern compile = Pattern.compile(bt_name + ".*?-\\[([\\s\\S]*?)\\]-");
                        Matcher matcher = compile.matcher(source_annex);
                        String guard = "";
                        if (matcher.find()) {
                            guard = matcher.group(1);
                            guard = guard.replaceAll("&#x9;", "");
                            guard = guard.replaceAll("&#xA;", "");
                            guard = guard.replaceAll("\n", " ");
                            guard = guard.replaceAll("\t", "");
//                            System.out.println(guard);
                            guard = guard.replaceAll("<", "&lt;");
                            bt_aadl.setGuard(guard);
                        }

                    }

                    //dispatch
                    Element dispatch = bt.getChild("dispatch");
                    if (dispatch != null) {
                        ;
                    }

                    //action
                    ArrayList<BUpdate> bu = new ArrayList<>();


                    Element behavior = bt.getChild("actions");
                    List<Element> action = null;
                    if (behavior != null) {
                        //normal
                        action = behavior.getChildren("action");

                        if (action != null) {

                            process_actions(ba, bu, action);
                        }
                    }
                    bt_aadl.setUpdate(bu);
                    ba.getTrans().add(bt_aadl);
                }
            }

            impl.getAnnexs().add(ba);

        } else if (annex_name.equals("Uncertainty")) {
            UncertaintyAnnex ua = new UncertaintyAnnex("");

            String sourceText = parsedAnnexSubclause.getAttributeValue("sourceText");
            sourceText = sourceText.replace("\t", "");
            sourceText = sourceText.replace("\r", "");
            sourceText = sourceText.replaceAll("--.*?\n", "\n");

            //var
            Pattern compile = Pattern.compile("variables([\\s\\S]*)distributions");
            Matcher matcher = compile.matcher(sourceText);
//            matcher.
            if (matcher.find()) {
                String variables = matcher.group(1);
//                System.out.println(variables);
//                time v_delay applied to Train.ts
//                        -- modeling connection delay
//                static price v_fr applied to Train.ta
//                        -- modeling rail friction
                String[] split = variables.split("\n");
                for (String line : split) {
                    //System.out.println("line"+line);
                    if (line.contains("time")) {
                        compile = Pattern.compile("time (.*?) applied to ([\\w\\.]*)");
                        matcher = compile.matcher(line);
                        matcher.find();
                        //System.out.println(matcher.group(1)+matcher.group(2));
                        UVar time_delay = new UVar(matcher.group(1), "time");
                        String[] comp_var = matcher.group(2).split("\\.");

                        APort port_by_name = Utils.find_port_by_name(amodel, comp_var[0], comp_var[1]);
                        time_delay.setApplied(port_by_name);

                        ua.getVars().add(time_delay);
                    } else if (line.contains("static price")) {
                        compile = Pattern.compile("static price (.*?) applied to ([\\w\\.]*)");
                        matcher = compile.matcher(line);
                        matcher.find();
                        //System.out.println(matcher.group(1)+matcher.group(2));
                        UVar time_delay = new UVar(matcher.group(1), "static price");
                        String[] comp_var = matcher.group(2).split("\\.");

                        APort port_by_name = Utils.find_port_by_name(amodel, comp_var[0], comp_var[1]);
                        if (port_by_name == null) {
                            time_delay.applied_var = comp_var[1];
                        } else {
                            time_delay.setApplied(port_by_name);
                        }
                        ua.getVars().add(time_delay);
                    } else if (line.contains("price")) {
                        compile = Pattern.compile("price (.*?) applied to ([\\w\\.]*)");
                        matcher = compile.matcher(line);
                        matcher.find();
                        //System.out.println(matcher.group(1)+matcher.group(2));
                        UVar time_delay = new UVar(matcher.group(1), "price");
                        String[] comp_var = matcher.group(2).split("\\.");

                        APort port_by_name = Utils.find_port_by_name(amodel, comp_var[0], comp_var[1]);
                        time_delay.setApplied(port_by_name);

                        ua.getVars().add(time_delay);
                    }
                }
            }

            //dist
            compile = Pattern.compile("distributions([\\s\\S]*)(queries)?");
            matcher = compile.matcher(sourceText);
//            matcher.
            if (matcher.find()) {
                String variables = matcher.group(1);

                String[] split = variables.split("\n");
                for (String line : split) {
                    compile = Pattern.compile("([\\w_]*)?\\s*?=\\s*?(\\w*?)\\(([\\d\\.-]*?),([\\d\\.-]*?)\\)");
                    matcher = compile.matcher(line);
                    if (matcher.find()) {
                        //System.out.println(matcher.group(1) + matcher.group(2) + matcher.group(3) + matcher.group(4));
                        Distribution dist = new Distribution();
                        dist.setDistName(matcher.group(2));

                        ArrayList<Double> params = new ArrayList<>();
                        params.add(Double.valueOf(matcher.group(3)));
                        params.add(Double.valueOf(matcher.group(4)));
                        dist.setParas(params);

                        for (UVar var : ua.getVars()) {
                            if (var.getName().equals(matcher.group(1))) {
                                var.dist = dist;
                            }
                        }
                        ua.getDists().add(dist);
                    }
                }
            }

            //query
            compile = Pattern.compile("queries([\\s\\S]*)");
            matcher = compile.matcher(sourceText);
//            matcher.
            if (matcher.find()) {
                String variables = matcher.group(1);

                String[] split = variables.split("\n");
                for (String line : split) {
                    if (line.contains("=")) {
                        ua.getQueries().add(line.substring(line.indexOf("=") + 1));
                    }
                }
            }

            impl.getAnnexs().add(ua);
        } else {
            throw new RuntimeException("not supprot annex");
        }
    }

    private void process_actions(BLESSAnnex ba, ArrayList<BUpdate> bu, List<Element> action) {
        for (Element act : action) {
            //precondition
            //act
            Element each_act = act.getChild("action");

            //for not basic -elq
            Element elq = each_act.getChild("elq");
            if (elq != null) {
                List<Element> children = elq.getChild("actions").getChildren("action");
                process_actions(ba, bu, children);

            }

            Element basic = each_act.getChild("basic");
            if (basic != null) {
                //communication
                Element communication = basic.getChild("communication");
                if (communication != null) {
                    //inport
                    if (communication.getChild("pi") != null) {
                        String port_name = communication.getChild("pi").getAttributeValue("port");
                        Element port_ele = (Element) xpfac.compile(Utils.convert2xpath(port_name), Filters.element()).evaluateFirst(doc.getRootElement());
                        port_name = port_ele.getAttributeValue("name");
                        String val = communication.getChild("pi").getChild("variable").getAttributeValue("id");
                        BVar varByName = Utils.getVarByName(val, ba.getVariables());
                        //TODO 应该用connection来查找
                        APort port_by_name = Utils.find_port_by_name(amodel, port_name);
                        if (port_by_name == null) {
                            //还没初始化出来
                            port_by_name = new APort(port_name, APort.out);
                        }
                        APort same_oppo_port = port_by_name.get_same_oppo_port();
                        bu.add(new BUpdate(null, same_oppo_port, varByName));
                    } else if (communication.getChild("po") != null) {
                        //outport
                        //event
                        String port_name = communication.getChild("po").getAttributeValue("port");
                        Element port_ele = (Element) xpfac.compile(Utils.convert2xpath(port_name), Filters.element()).evaluateFirst(doc.getRootElement());
                        port_name = port_ele.getAttributeValue("name");
                        APort port_by_name = Utils.find_port_by_name(amodel, port_name);
                        if (port_by_name == null) {
                            //还没初始化出来
                            port_by_name = new APort(port_name, APort.in);
                        }
                        APort same_oppo_port = port_by_name.get_same_oppo_port();

                        //data
                        //Attribute val = (Attribute) xpfac.compile("//pn/@identifier", Filters.attribute()).evaluateFirst(communication);
                        Element eor = communication.getChild("po").getChild("eor");
                        String val = null;
                        boolean minus = false;
                        if (eor != null) {
                            Element child = eor.getChild("exp").getChild("se");

                            String minus1 = child.getAttributeValue("minus", "false");
                            if (minus1.equals("true")) {
                                minus = true;
                            }
                            Element variable = child.getChild("value").getChild("variable");
                            if (variable != null) {
                                val = variable.getAttributeValue("id");
                            }
                            Element const_v = child.getChild("value").getChild("const");
                            if (const_v != null) {
                                //sb:rate
                                //val=
                            }
                        }

                        if (val != null) {
                            BVar varByName = Utils.getVarByName(val, ba.getVariables());
                            if (minus) {
                                varByName.setInitVal(varByName.getInitVal() * -1);
                            }
                            bu.add(new BUpdate(null, same_oppo_port, varByName));
                        } else {
                            bu.add(new BUpdate(null, same_oppo_port, null));
                        }
                    }

                }
                //mutil_assgin

            }
            //postcondition
        }
    }

    private void process_date_port(ACompoentDeclare ab_declare, Element port) {
        ADataPort dp = new ADataPort(port.getAttributeValue("name"));
        if (port.getAttributeValue("out", "false").equals("true")) {
            dp.setDirection(APort.out);
        } else {
            dp.setDirection(APort.in);
        }
        String port_type = port.getAttributeValue("dataFeatureClassifier");
        port_type = port_type.substring(port_type.indexOf("#") + 1).replace(".", "::");
        dp.setType(port_type);
        ab_declare.ports.add(dp);
    }

    private void process_event_port(ACompoentDeclare ab_declare, Element port) {
        AEventPort ep = new AEventPort(port.getAttributeValue("name"));
        if (port.getAttributeValue("out", "false").equals("true")) {
            ep.setDirection(APort.out);
        } else {
            ep.setDirection(APort.in);
        }
        ab_declare.ports.add(ep);
    }

    public static void main(String[] args) throws Exception {
        AAXLParser3 aaxlParser3 = new AAXLParser3(new File(
                "src/aadl2upaal/parser/MA.xml"));
        aaxlParser3.createAADLModel();
    }
}
