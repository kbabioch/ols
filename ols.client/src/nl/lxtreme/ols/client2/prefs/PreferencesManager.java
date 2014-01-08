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
 * Copyright (C) 2010-2014 J.W. Janssen, www.lxtreme.nl
 */
package nl.lxtreme.ols.client2.prefs;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

import javax.swing.*;

import nl.lxtreme.ols.util.swing.*;

import org.osgi.service.cm.*;
import org.osgi.service.log.*;


/**
 * Provides a {@link ManagedService} that takes care of the UI-preferences.
 */
public class PreferencesManager implements ManagedService
{
  // CONSTANTS

  public static final String PID = "ols.ui.defaults";

  private static final String PREFIX = "ols.";
  private static final String FONT_SUFFIX = ".font";
  private static final String COLOR_SUFFIX = ".color";
  private static final String FLOAT_SUFFIX = ".float";
  private static final String ENUM_SUFFIX = ".enum";
  private static final String BOOLEAN_SUFFIX = ".boolean";

  private static final Pattern FONT_PATTERN = Pattern
      .compile( "\\s*(%\\{[^}]+\\}|[\\s]+)\\s*(?i)(bold|italic|plain)?\\s*([^\\s]+)?\\s*" );

  // VARIABLES

  // Injected by Felix DM...
  private volatile LogService log;

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "rawtypes" )
  public void updated( Dictionary aProperties ) throws ConfigurationException
  {
    // Fall back to the defaults by removing all keys from UIManager...
    removeOlsSpecificKeys();

    if ( aProperties != null )
    {
      // Apply the specific values to the UIManager...
      applyOlsSpecificKeys( aProperties );
    }
  }

  /**
   * @param aResult
   * @param aSize
   * @return
   */
  private Font applyFontSize( final Font aResult, final String aSize )
  {
    if ( aSize.endsWith( "em" ) )
    {
      // Relative size...
      float factor = Float.parseFloat( aSize.substring( 0, aSize.length() - 2 ) );
      return aResult.deriveFont( aResult.getSize2D() * factor );
    }
    else if ( aSize.endsWith( "%" ) )
    {
      // Percentage...
      float factor = Float.parseFloat( aSize.substring( 0, aSize.length() - 1 ) );
      return aResult.deriveFont( aResult.getSize2D() * ( factor / 100.0f ) );
    }
    else
    {
      // Points...
      int pointSize = Integer.parseInt( aSize );
      return aResult.deriveFont( pointSize );
    }
  }

  /**
   * @param aResult
   * @param aVariant
   * @return
   */
  private Font applyFontVariant( final Font aResult, final String aVariant )
  {
    if ( "bold".equalsIgnoreCase( aVariant ) )
    {
      return aResult.deriveFont( Font.BOLD );
    }
    else if ( "italic".equalsIgnoreCase( aVariant ) || "oblique".equalsIgnoreCase( aVariant ) )
    {
      return aResult.deriveFont( Font.ITALIC );
    }
    return aResult;
  }

  /**
   * @param aProperties
   * @throws ConfigurationException
   */
  private void applyOlsSpecificKeys( final Dictionary<?, ?> aProperties ) throws ConfigurationException
  {
    this.log.log( LogService.LOG_DEBUG, "Parsing OLS-specific properties..." );

    UIDefaults defaults = UIManager.getDefaults();

    this.log.log( LogService.LOG_DEBUG, "Applying OLS-specific properties..." );

    Properties config = parseValues( aProperties );

    // Write all values to the UIManager...
    for ( Map.Entry<Object, Object> entry : config.entrySet() )
    {
      Object key = entry.getKey();
      Object value = entry.getValue();

      defaults.put( key, value );
    }
  }

  /**
   * @param value
   * @return
   */
  private Font parseFontValue( String value )
  {
    Matcher m = FONT_PATTERN.matcher( value );
    if ( m.matches() )
    {
      String name = m.group( 1 );
      String variant = m.group( 2 );
      String size = m.group( 3 );

      Font result;
      if ( name.startsWith( "%" ) )
      {
        result = UIManager.getFont( name.substring( 2, name.length() - 1 ) );
      }
      else
      {
        result = Font.decode( name );
      }

      if ( size != null )
      {
        result = applyFontSize( result, size );
      }

      if ( variant != null )
      {
        result = applyFontVariant( result, variant );
      }

      return result;
    }
    return null;
  }

  /**
   * @param aKey
   * @param aObject
   * @return
   */
  private Object parseValue( Dictionary<?, ?> aConfig, String aKey )
  {
    String value = ( ( String )aConfig.get( aKey ) ).trim();
    if ( "".equals( value ) )
    {
      return null;
    }

    if ( value.startsWith( "${" ) && value.endsWith( "}" ) )
    {
      String lookup = value.substring( 2, value.length() - 1 );
      return parseValue( aConfig, lookup );
    }

    if ( aKey.endsWith( BOOLEAN_SUFFIX ) )
    {
      // Parse as boolean value...
      return Boolean.valueOf( value );
    }
    else if ( aKey.endsWith( COLOR_SUFFIX ) )
    {
      // Parse as color; allowing empty values to be supplied...
      return ColorUtils.parseColor( value );
    }
    else if ( aKey.endsWith( ENUM_SUFFIX ) )
    {
      // Parse as String-enum value...
      return value;
    }
    else if ( aKey.endsWith( FLOAT_SUFFIX ) )
    {
      // Parse as float value
      return Float.valueOf( value );
    }
    else if ( aKey.endsWith( FONT_SUFFIX ) )
    {
      // Parse as Font value...
      return parseFontValue( value );
    }
    else
    {
      // Parse as integer value
      return Integer.valueOf( value );
    }
  }

  /**
   * Parses a given dictionary of (String) values and returns a
   * {@link Properties} object with concrete values.
   * 
   * @param aDictionary
   *          the dictionary to parse, cannot be <code>null</code>.
   * @return the parsed properties, never <code>null</code>.
   * @throws ConfigurationException
   *           in case an invalid value was found.
   */
  private Properties parseValues( Dictionary<?, ?> aDictionary ) throws ConfigurationException
  {
    Properties config = new Properties();

    Enumeration<?> keys = aDictionary.keys();
    while ( keys.hasMoreElements() )
    {
      String key = ( String )keys.nextElement();
      if ( !key.startsWith( PREFIX ) )
      {
        continue;
      }

      try
      {
        Object value = parseValue( aDictionary, key );
        if ( value != null )
        {
          config.put( key, value );
        }
      }
      catch ( Exception exception )
      {
        this.log.log( LogService.LOG_ERROR,
            "Configuration problem for '" + key + "' (value = '" + aDictionary.get( key ) + "')!", exception );
        throw new ConfigurationException( key, "Unable to parse value!", exception );
      }
    }

    // Replace all placeholders with real values...
    replacePlaceholders( config );
    return config;
  }

  /**
   * Removes all OLS-specific properties from the {@link UIManager}.
   */
  private void removeOlsSpecificKeys()
  {
    UIDefaults defaults = UIManager.getDefaults();

    this.log.log( LogService.LOG_DEBUG, "Removing OLS-specific properties..." );

    Enumeration<Object> keys = defaults.keys();
    while ( keys.hasMoreElements() )
    {
      Object key = keys.nextElement();
      if ( key.toString().startsWith( PREFIX ) )
      {
        defaults.put( key, null );
      }
    }
  }

  /**
   * @param aConfig
   * @throws ConfigurationException
   */
  private void replacePlaceholders( final Properties aConfig ) throws ConfigurationException
  {
    final List<Object> keySet = new ArrayList<Object>( aConfig.keySet() );
    for ( Object key : keySet )
    {
      Object value = aConfig.get( key );
      if ( ( value instanceof String ) && ( ( String )value ).matches( "\\$\\{[^}]+\\}" ) )
      {
        String v = ( String )value;
        v = v.substring( 2, v.length() - 1 );

        Object newValue = aConfig.get( v );
        if ( newValue == null )
        {
          throw new ConfigurationException( key.toString(), "Missing value for placeholder!" );
        }
        aConfig.put( key, newValue );
      }
    }
  }
}