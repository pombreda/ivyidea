/*
 * Copyright 2009 Guy Mahieu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.clarent.ivyidea;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.clarent.ivyidea.exception.IvyFileReadException;
import org.clarent.ivyidea.exception.IvySettingsFileReadException;
import org.clarent.ivyidea.exception.IvySettingsNotFoundException;
import org.clarent.ivyidea.intellij.IntellijUtils;
import org.clarent.ivyidea.intellij.task.IvyIdeaResolveBackgroundTask;
import org.clarent.ivyidea.ivy.IvyManager;
import org.clarent.ivyidea.resolve.IntellijDependencyResolver;
import org.clarent.ivyidea.resolve.dependency.ResolvedDependency;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.List;

/**
 * Action to resolve the dependencies for the active module.
 *
 * @author Guy Mahieu
 */
public class ResolveForActiveModuleAction extends AbstractResolveAction {

    private static final String MENU_TEXT = "Resolve for {0} module";

    public void actionPerformed(final AnActionEvent e) {
        final Module module = DataKeys.MODULE.getData(e.getDataContext());
        if (module != null) {
            ProgressManager.getInstance().run(new IvyIdeaResolveBackgroundTask(module.getProject(), e) {

                public void doResolve(@NotNull ProgressIndicator progressIndicator) throws IvySettingsNotFoundException, IvyFileReadException, IvySettingsFileReadException {
                    clearConsole(myProject);
                    final IntellijDependencyResolver resolver = new IntellijDependencyResolver(new IvyManager());
                    final List<ResolvedDependency> list = resolver.resolve(module);
                    updateIntellijModel(module, list);
                    reportProblems(module, resolver.getProblems());
                }
            });
        }
    }

    public void update(AnActionEvent e) {
        final Module activeModule = DataKeys.MODULE.getData(e.getDataContext());
        updatePresentation(e.getPresentation(), activeModule);
    }

    private void updatePresentation(Presentation presentation, Module activeModule) {
        boolean linkEnabled = IntellijUtils.containsIvyIdeaFacet(activeModule);
        presentation.setText(MessageFormat.format(MENU_TEXT, linkEnabled ? activeModule.getName() : "active"));
        presentation.setEnabled(linkEnabled);
        presentation.setVisible(linkEnabled);
    }
}