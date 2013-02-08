package com.example.reformedforumxmltester;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase();
                case 1:
                    return getString(R.string.title_section2).toUpperCase();
                case 2:
                    return getString(R.string.title_section3).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Create a new TextView and set its text to the fragment's section
            // number argument value.
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }

	public enum rfXml {
		TEXT, TITLE, TYPE, XMLURL, SEARCHURL, HTMLURL;    		
	}
    
    public static class rfXMLParser {
    	public static final String ns = null;
    	
    	public List parse(InputStream in) throws XmlPullParserException, IOException {
    		try {
    			XmlPullParser parser = Xml.newPullParser();
    			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
    			parser.setInput(in,  null);
    			parser.nextTag();
    			return readFeed(parser);
    		} finally {
    			in.close();
    		}
    	}
    	public List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    		List entries = new ArrayList();
    		
    		parser.require(XmlPullParser.START_TAG, ns, "feed");
    		while (parser.next() != XmlPullParser.END_TAG) {
    	        if (parser.getEventType() != XmlPullParser.START_TAG) {
    	            continue;
    	        }
    	        String name = parser.getName();
    	        // Starts by looking for the entry tag
    	        if (name.equals("program")) {
    	            entries.add(readProgram(parser));
    	        } else {
    	            skip(parser);
    	        }
    		}
    		return entries;
    	}
   	
    	private rfProgram readProgram(XmlPullParser parser) throws XmlPullParserException, IOException {
            parser.require(XmlPullParser.START_TAG, ns, "program");
            String text = null, title = null, type = null , xmlUrl = null, searchUrl=null, htmlUrl = null;

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                switch(rfXml.valueOf(name)) {
                case TEXT: 
                	text = readTag(parser, name);
                case TITLE: 
                	title = readTag(parser, name);
               	case TYPE:
                	type = readTag(parser, name);
                //case XMLURL: case SEARCHURL: case HTMLURL:
                 //	text = readTag(parser, name);
                default:
                	skip(parser);
                }
            }
            return new rfProgram(text, title, type, xmlUrl, searchUrl, htmlUrl);
        }
    	
    	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    	    if (parser.getEventType() != XmlPullParser.START_TAG) {
    	        throw new IllegalStateException();
    	    }
    	    int depth = 1;
    	    while (depth != 0) {
    	        switch (parser.next()) {
    	        case XmlPullParser.END_TAG:
    	            depth--;
    	            break;
    	        case XmlPullParser.START_TAG:
    	            depth++;
    	            break;
    	        }
    	    }
    	 }
    	
    	private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
    	    parser.require(XmlPullParser.START_TAG, ns, tag);
    	    String title = readText(parser);
    	    parser.require(XmlPullParser.END_TAG, ns, tag);
    	    return title;
    	}
    	
    	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    	    String result = "";
    	    if (parser.next() == XmlPullParser.TEXT) {
    	        result = parser.getText();
    	        parser.nextTag();
    	    }
    	    return result;
    	}
    }
    
    
    public static class rfProgram {
    	public final String text, title, type, xmlUrl, searchUrl, htmlUrl;
    
    	private rfProgram(String text, String title, String type, String xmlUrl, String searchUrl, String htmlUrl){
    		this.text = text;
    		this.title = title;
    		this.type = type;
    		this.xmlUrl = xmlUrl;
    		this.searchUrl = searchUrl;
    		this.htmlUrl = htmlUrl;
    			
    	}    	  
    }
    }

