/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jdo.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;
import org.ecocean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


public class SiteSearch extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(SiteSearch.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }


    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String context="context0";
        context=ServletUtilities.getContext(request);
        Shepherd myShepherd = new Shepherd(context);
        //set up for response
        response.setContentType("text/json");
        PrintWriter out = response.getWriter();

        String term = request.getParameter("term");
        if ((term == null) || term.equals("")) {
            out.println("[]");
            return;
        }

        String regex = ".*" + term.toLowerCase() + ".*";

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        String filter;
        Query query;

        //
        // Query on Individuals
        //
        filter = "this.nickName.toLowerCase().matches('"
                + regex
                + "') || this.individualID.matches('"
                + regex + "')";

        query = myShepherd.getPM().newQuery(MarkedIndividual.class);
        query.setFilter(filter);

        //
        // Check to make sure our query is fine, log error if not.
        //
        if (logger.isDebugEnabled()) {
            logger.debug(filter);
            try {
                query.compile();
            } catch (Throwable ex) {
                logger.error("Bad query", ex);
            }
        }

        @SuppressWarnings("unchecked")
        List<MarkedIndividual> individuals = (List<MarkedIndividual>) query.execute();

        for (MarkedIndividual ind : individuals) {
            HashMap<String, String> hm = new HashMap<String, String>();
            if (StringUtils.isBlank(ind.getNickName())) {
                hm.put("label", ind.getIndividualID());
            } else {
                hm.put("label", ind.getNickName() + " (" + ind.getIndividualID() + ")");
            }
            hm.put("value", ind.getIndividualID());
            hm.put("type", "individual");
            list.add(hm);
        }

        //
        // Query on Users
        //
        filter = "this.fullName.toLowerCase().matches('"
                + regex
                + "') || this.username.toLowerCase().matches('"
                + regex + "')";

        query = myShepherd.getPM().newQuery(User.class);
        query.setFilter(filter);

        @SuppressWarnings("unchecked")
        List<User> users = (List<User>) query.execute();

        for (User user : users) {
            HashMap<String, String> hm = new HashMap<String, String>();
            if (StringUtils.isBlank(user.getFullName())) {
                hm.put("label", user.getUsername());
            } else {
                hm.put("label", user.getFullName() + " (" + user.getUsername() + ")");
            }
            hm.put("value", user.getUsername());
            hm.put("type", "user");
            list.add(hm);
        }

        //
        // return our results
        //
        out.println(new Gson().toJson(list));
    }
}