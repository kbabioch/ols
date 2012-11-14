/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010-2012 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client;


import java.util.*;

import nl.lxtreme.ols.common.*;
import nl.lxtreme.ols.common.acquisition.*;
import nl.lxtreme.ols.common.session.*;


/**
 * Provides a front-end controller for dealing with cursor functionality.
 */
public final class CursorController
{
  // VARIABLES

  // Injected by Felix DM...
  private volatile Session session;

  // METHODS

  /**
   * Provides direct access to the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor, >= 0 && < {@link Ols#MAX_CURSORS}.
   * @return a cursor, never <code>null</code>.
   */
  public Cursor getCursor( final int aCursorIdx )
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData == null )
    {
      return null;
    }

    return acquisitionData.getCursor( aCursorIdx );
  }

  /**
   * Returns an array of all defined cursors, that is, all cursors that are
   * defined with a valid timestamp.
   * 
   * @return an array of defined cursors, never <code>null</code>.
   */
  public Cursor[] getDefinedCursors()
  {
    List<Cursor> cursors = new ArrayList<Cursor>();

    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData != null )
    {
      for ( int i = 0; i < Ols.MAX_CURSORS; i++ )
      {
        Cursor cursor = acquisitionData.getCursor( i );
        if ( ( cursor != null ) && cursor.isDefined() )
        {
          cursors.add( cursor );
        }
      }
    }

    return cursors.toArray( new Cursor[cursors.size()] );
  }

  /**
   * Returns whether or not the cursor with the given index is defined.
   * 
   * @param aCursorIdx
   *          the index of the cursor to test, >= 0;
   * @return <code>true</code> if the cursor with the given index has a defined
   *         timestamp, <code>false</code> otherwise.
   */
  public boolean isCursorDefined( final int aCursorIdx )
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData == null )
    {
      return false;
    }

    final Cursor cursor = acquisitionData.getCursor( aCursorIdx );
    return ( cursor != null ) && cursor.isDefined();
  }

  /**
   * Returns whether or not the cursors are visible on screen.
   * 
   * @return <code>true</code> if cursors are visible on screen,
   *         <code>false</code> if they are hidden from the screen.
   */
  public boolean isCursorsVisible()
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData == null )
    {
      return false;
    }

    return acquisitionData.isCursorsVisible();
  }

  /**
   * Sets the timestamp of the cursor with the given index to the given
   * timestamp.
   * 
   * @param aCursorIdx
   *          the index of the cursor to move, >= 0;
   * @param aTimestamp
   *          the new timestamp of the cursor, >= 0L.
   */
  public void moveCursor( final int aCursorIdx, final long aTimestamp )
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData != null )
    {
      final Cursor cursor = acquisitionData.getCursor( aCursorIdx );
      if ( cursor != null )
      {
        cursor.setTimestamp( aTimestamp );
      }
    }
  }

  /**
   * Clears the timestamp of the cursor with the given index.
   * 
   * @param aCursorIdx
   *          the index of the cursor to move, >= 0.
   */
  public void removeCursor( final int aCursorIdx )
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData != null )
    {
      final Cursor cursor = acquisitionData.getCursor( aCursorIdx );
      if ( cursor != null )
      {
        cursor.clear();
      }
    }
  }

  /**
   * Sets whether or not the (defined) cursors are visible on screen.
   * 
   * @param aVisible
   *          <code>true</code> if the cursors are visible on screen,
   *          <code>false</code> if they are to be hidden from the screen.
   */
  public void setCursorsVisible( final boolean aVisible )
  {
    AcquisitionData acquisitionData = getAcquisitionData();
    if ( acquisitionData != null )
    {
      acquisitionData.setCursorsVisible( aVisible );
    }
  }

  /**
   * @return the current {@link AcquisitionData}, can be <code>null</code>.
   */
  private AcquisitionData getAcquisitionData()
  {
    return this.session.getAcquisitionData();
  }
}
