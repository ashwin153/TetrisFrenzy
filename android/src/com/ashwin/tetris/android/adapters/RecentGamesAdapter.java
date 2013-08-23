package com.ashwin.tetris.android.adapters;

import java.util.ArrayList;

import com.ashwin.tetris.android.R;
import com.ashwin.tetris.android.R.color;
import com.ashwin.tetris.android.R.id;
import com.ashwin.tetris.android.R.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecentGamesAdapter extends BaseAdapter {

	private ArrayList<TetrisGame> _recentGames;
	private Context _context;
	private Typeface _typeface;
	
	public RecentGamesAdapter(Context context, Typeface typeface, ArrayList<TetrisGame> recentGames) {
		_recentGames = recentGames;
		_context = context;
		_typeface = typeface;
	}
	
	@Override
	public int getCount() {
		return _recentGames.size();
	}

	@Override
	public Object getItem(int position) {
		return _recentGames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        GamesHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity) _context).getLayoutInflater();
            row = inflater.inflate(R.layout.list_view_games, parent, false);
            
            holder = new GamesHolder();
            holder._opponent = (TextView) row.findViewById(R.id.ListViewGames_opponent);
            holder._date = (TextView) row.findViewById(R.id.ListViewGames_date);
            
            row.setTag(holder);
            setTypeface(row, _typeface);
        }
        else
        {
            holder = (GamesHolder) row.getTag();
        }
        
        TetrisGame game = _recentGames.get(position);
        if(game.isWon()) {
        	holder._opponent.setTextColor(_context.getResources().getColor(R.color.green));
        	holder._date.setTextColor(_context.getResources().getColor(R.color.green));
        } else {
        	holder._opponent.setTextColor(_context.getResources().getColor(R.color.red));
        	holder._date.setTextColor(_context.getResources().getColor(R.color.red));
        }
        
        holder._opponent.setText(game.getOpponent());
        holder._date.setText(game.getTimeString());
        
        return row;
	}
	
	// Sets the typeface for a ViewGroup and all child ViewGroups
	private void setTypeface(View view, Typeface tf) {
		if(!(view instanceof ViewGroup)) {
			if(view instanceof TextView)
				((TextView) view).setTypeface(tf);
			return;
		}
		
		for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			setTypeface(((ViewGroup) view).getChildAt(i), tf);
	}

    static class GamesHolder
    {
        TextView _opponent, _date;
    }
}
