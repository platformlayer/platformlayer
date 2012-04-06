//package org.platformlayer.client.cli.commands;
//
//import java.io.PrintWriter;
//
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//import org.kohsuke.args4j.Argument;
//import org.platformlayer.Format;
//import org.platformlayer.PlatformLayerClient;
//import org.platformlayer.PlatformLayerClientException;
//import org.platformlayer.client.cli.autocomplete.AutoCompleteItemType;
//import org.platformlayer.core.model.PlatformLayerKey;
//
//import com.fathomdb.cli.autocomplete.AutoCompletor;
//import com.fathomdb.cli.autocomplete.SimpleAutoCompleter;
//
//public class CreateItem extends PlatformLayerCommandRunnerBase {
//    @Argument(index = 0)
//    public String path;
//
//    @Argument(index = 1)
//    public String json;
//
//    public CreateItem() {
//        super("create", "item");
//    }
//
//    @Override
//    public Object runCommand() throws PlatformLayerClientException, JSONException {
//        PlatformLayerClient client = getPlatformLayerClient();
//
//        PlatformLayerKey key = pathToKey(client, path);
//
//        String wrapper = "{ \"" + key.getItemType().getKey() + "\": " + json + " }";
//        String retvalJsonString = client.createItem(key.getServiceType(), key.getItemType(), wrapper, Format.JSON);
//        JSONObject retvalJsonObject = new JSONObject(retvalJsonString);
//
//        return retvalJsonObject;
//    }
//
//    @Override
//    public AutoCompletor getAutoCompleter() {
//        return new SimpleAutoCompleter(new AutoCompleteItemType(), null);
//    }
//
//    @Override
//    public void formatRaw(Object o, PrintWriter writer) {
//        JSONObject json = (JSONObject) o;
//        try {
//            writer.println(json.getString("id"));
//        } catch (JSONException e) {
//            throw new IllegalArgumentException("id property not found");
//        }
//    }
//
// }
