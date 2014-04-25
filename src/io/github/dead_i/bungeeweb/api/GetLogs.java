package io.github.dead_i.bungeeweb.api;

import com.google.gson.Gson;
import io.github.dead_i.bungeeweb.APICommand;
import io.github.dead_i.bungeeweb.BungeeWeb;
import net.md_5.bungee.api.plugin.Plugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class GetLogs extends APICommand {
    private Gson gson = new Gson();

    public GetLogs() {
        super("getlogs", 1);
    }

    @Override
    public void execute(Plugin plugin, HttpServletRequest req, HttpServletResponse res, String[] args) throws IOException {
        if (!checkPermission(req, res)) return;
        ArrayList<String> conditions = new ArrayList<String>();
        ArrayList<Object> params = new ArrayList<Object>();

        String player = req.getParameter("uuid");
        if (player != null) {
            conditions.add("`uuid`=?");
            params.add(player);
        }

        String from = req.getParameter("time");
        if (from != null && BungeeWeb.isNumber(from)) {
            conditions.add("`time`>?");
            params.add(Integer.parseInt(from));
        }

        String qry = "SELECT * FROM `" + BungeeWeb.getConfig().getString("database.prefix") + "logs` ";

        if (conditions.size() > 0) {
            String cond = "WHERE ";
            for (String s : conditions) {
                cond += s + " AND ";
            }
            qry += cond.substring(0, cond.length() - 4);
        }

        String limit = req.getParameter("limit");
        if (limit != null && BungeeWeb.isNumber(limit)) {
            qry += "LIMIT " + Math.floor(Integer.parseInt(limit));
        }

        ArrayList<Object> out = new ArrayList<Object>();
        try {
            PreparedStatement st = BungeeWeb.getDatabase().prepareStatement(qry);
            int i = 0;
            for (Object o : params) {
                i++;
                st.setObject(i, o);
            }
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> record = new HashMap<String, Object>();
                record.put("time", rs.getInt("time"));
                record.put("type", rs.getInt("type"));
                record.put("uuid", rs.getString("uuid"));
                record.put("username", rs.getString("username"));
                record.put("content", rs.getString("content"));
                out.add(record);
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        res.getWriter().print(gson.toJson(out));
    }
}