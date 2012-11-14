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
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client.action;


import java.awt.event.*;

import javax.swing.*;

import nl.lxtreme.ols.client.*;
import nl.lxtreme.ols.common.session.*;


/**
 * Removes all current annotations from the current signal display.
 */
public class RemoveAnnotationsAction extends AbstractAction implements IManagedAction
{
  // CONSTANTS

  private static final long serialVersionUID = 1L;

  public static final String ID = "RemoveAnnotations";

  // CONSTRUCTORS

  /**
   * Creates a new {@link RemoveAnnotationsAction} instance.
   */
  public RemoveAnnotationsAction()
  {
    putValue( NAME, "Remove annotations" );
    putValue( SHORT_DESCRIPTION, "Removes all existing annotations from the signal display." );
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void actionPerformed( final ActionEvent aEvent )
  {
    Session session = Client.getInstance().getSession();

    session.getAnnotationData().clearAll();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getId()
  {
    return ID;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateState()
  {
    setEnabled( hasCapturedData() );
  }

  /**
   * @return <code>true</code> if there is data captured to export,
   *         <code>false</code> otherwise.
   */
  private boolean hasCapturedData()
  {
    final Session session = Client.getInstance().getSession();
    return session.hasData();
  }
}
