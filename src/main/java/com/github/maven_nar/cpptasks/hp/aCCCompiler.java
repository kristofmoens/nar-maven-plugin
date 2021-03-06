/*
 * #%L
 * Native ARchive plugin for Maven
 * %%
 * Copyright (C) 2002 - 2014 NAR Maven Plugin developers.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.maven_nar.cpptasks.hp;

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.types.Environment;

import com.github.maven_nar.cpptasks.CUtil;
import com.github.maven_nar.cpptasks.OptimizationEnum;
import com.github.maven_nar.cpptasks.compiler.LinkType;
import com.github.maven_nar.cpptasks.compiler.Linker;
import com.github.maven_nar.cpptasks.gcc.GccCompatibleCCompiler;

/**
 * Adapter for the HP aC++ C++ compiler
 *
 * @author Curt Arnold
 */
public final class aCCCompiler extends GccCompatibleCCompiler {
  private final static String[] headerExtensions = new String[] {
      ".h", ".hpp", ".inl"
  };
  private final static String[] sourceExtensions = new String[] {
      ".c", ".cc", ".cxx", ".cpp", ".c++", ".i", ".s"
  };

  private static final aCCCompiler instance = new aCCCompiler("aCC", sourceExtensions, headerExtensions, false, null);

  /**
   * Gets singleton instance of this class
   */
  public static aCCCompiler getInstance() {
    return instance;
  }

  private String identifier;
  private File[] includePath;

  /**
   * Private constructor. Use GccCCompiler.getInstance() to get singleton
   * instance of this class.
   */
  private aCCCompiler(final String command, final String[] sourceExtensions, final String[] headerExtensions,
      final boolean newEnvironment, final Environment env) {
    super(command, "-help", sourceExtensions, headerExtensions, false, null, newEnvironment, env);
  }

  @Override
  public void addImpliedArgs(final Vector<String> args, final boolean debug, final boolean multithreaded,
      final boolean exceptions, final LinkType linkType, final Boolean rtti, final OptimizationEnum optimization) {
    args.addElement("-c");
    if (debug) {
      args.addElement("-g");
    }
    /*
     * if (multithreaded) { args.addElement("-mt"); }
     */

    //
    // per patch 1193690
    //
    if (linkType.isSharedLibrary() && !args.contains("+Z")) {
      args.addElement("+z");
    }
  }

  @Override
  public void addWarningSwitch(final Vector<String> args, final int level) {
    switch (level) {
      case 0:
        args.addElement("-w");
        break;
      case 1:
      case 2:
        args.addElement("+w");
        break;
    /*
     * case 3: case 4: case 5: args.addElement("+w2"); break;
     */
    }
  }

  @Override
  public File[] getEnvironmentIncludePath() {
    if (this.includePath == null) {
      final File ccLoc = CUtil.getExecutableLocation("aCC");
      if (ccLoc != null) {
        final File compilerIncludeDir = new File(new File(ccLoc, "../include").getAbsolutePath());
        if (compilerIncludeDir.exists()) {
          this.includePath = new File[2];
          this.includePath[0] = compilerIncludeDir;
        }
      }
      if (this.includePath == null) {
        this.includePath = new File[1];
      }
      this.includePath[this.includePath.length - 1] = new File("/usr/include");
    }
    return this.includePath;
  }

  @Override
  public Linker getLinker(final LinkType linkType) {
    return aCCLinker.getInstance().getLinker(linkType);
  }

  @Override
  public int getMaximumCommandLength() {
    return Integer.MAX_VALUE;
  }
}
