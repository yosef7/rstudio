/*
 * NewDirectoryNavigationPage.java
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.projects.ui.newproject;

import java.util.ArrayList;

import org.rstudio.core.client.CommandWithArg;
import org.rstudio.core.client.js.JsUtil;
import org.rstudio.core.client.widget.HasWizardPageSelectionHandler;
import org.rstudio.core.client.widget.WizardNavigationPage;
import org.rstudio.core.client.widget.WizardPage;
import org.rstudio.core.client.widget.WizardProjectTemplatePage;
import org.rstudio.core.client.widget.WizardResources;
import org.rstudio.studio.client.RStudioGinjector;
import org.rstudio.studio.client.projects.model.NewProjectInput;
import org.rstudio.studio.client.projects.model.NewProjectResult;
import org.rstudio.studio.client.projects.model.ProjectTemplateDescription;
import org.rstudio.studio.client.projects.model.ProjectTemplateRegistry;
import org.rstudio.studio.client.projects.model.ProjectTemplateRegistryProvider;
import org.rstudio.studio.client.workbench.model.SessionInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NewDirectoryNavigationPage 
            extends WizardNavigationPage<NewProjectInput,NewProjectResult>
{

   public NewDirectoryNavigationPage(SessionInfo sessionInfo)
   {
      super("New Directory", 
            "Start a project in a brand new working directory",
            "Project Type",
            NewProjectResources.INSTANCE.newProjectDirectoryIcon(),
            NewProjectResources.INSTANCE.newProjectDirectoryIconLarge(),
            createPages(sessionInfo),
            new WizardNavigationPageProducer<NewProjectInput, NewProjectResult>()
            {
               @Override
               public Widget createMainWidget(ArrayList<WizardPage<NewProjectInput, NewProjectResult>> pages)
               {
                  return createWidget(pages);
               }
            });
   }

   private static ArrayList<WizardPage<NewProjectInput, NewProjectResult>>
                                         createPages(SessionInfo sessionInfo)
   {
      ArrayList<WizardPage<NewProjectInput, NewProjectResult>> pages = 
            new ArrayList<WizardPage<NewProjectInput, NewProjectResult>>();
      
      // add default RStudio dialogs
      pages.add(new NewDirectoryPage());
      pages.add(new NewPackagePage());
      pages.add(new NewShinyAppPage());
      
      // add user-defined project template dialogs
      ProjectTemplateRegistryProvider registryProvider =
            RStudioGinjector.INSTANCE.getProjectTemplateRegistryProvider();
      ProjectTemplateRegistry registry = registryProvider.getProjectTemplateRegistry();
      for (String key : JsUtil.asIterable(registry.keys()))
      {
         JsArray<ProjectTemplateDescription> descriptions = registry.get(key);
         for (ProjectTemplateDescription description : JsUtil.asIterable(descriptions))
            pages.add(new WizardProjectTemplatePage(description));
      }
      
      return pages;
   }
   
   private static Widget createWidget(ArrayList<WizardPage<NewProjectInput, NewProjectResult>> pages)
   {
      return new Selector(pages);
   }
   
   private static class Selector
         extends Composite
         implements HasWizardPageSelectionHandler<NewProjectInput, NewProjectResult>
   {
      public Selector(final ArrayList<WizardPage<NewProjectInput, NewProjectResult>> pages)
      {
         WizardResources.Styles styles = WizardResources.INSTANCE.styles();
         
         ScrollPanel scrollPanel = new ScrollPanel();
         scrollPanel.setSize("100%", "100%");
         scrollPanel.addStyleName(styles.wizardPageSelector());

         VerticalPanel verticalPanel = new VerticalPanel();
         verticalPanel.setSize("100%", "100%");

         for (int i = 0, n = pages.size(); i < n; i++)
         {
            final WizardPage<NewProjectInput, NewProjectResult> page = pages.get(i);
            SelectorItem item = new SelectorItem(page, new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  onSelected_.execute(page);
               }
            });
            verticalPanel.add(item);
         }

         scrollPanel.add(verticalPanel);
         initWidget(scrollPanel);
      }
      
      @Override
      public void setSelectionHandler(CommandWithArg<WizardPage<NewProjectInput, NewProjectResult>> onSelected)
      {
         onSelected_ = onSelected;
      }
      
      private CommandWithArg<WizardPage<NewProjectInput, NewProjectResult>> onSelected_;
   }
   
   private static class SelectorItem extends Composite
   {
      public SelectorItem(WizardPage<NewProjectInput, NewProjectResult> page,
                          ClickHandler handler)
      {
         WizardResources.Styles styles = WizardResources.INSTANCE.styles();
         
         DockLayoutPanel panel = new DockLayoutPanel(Unit.PX);
         panel.addStyleName(styles.wizardPageSelectorItem());
         
         panel.setSize("100%", "24px");
         
         Image rightArrow = new Image(WizardResources.INSTANCE.wizardDisclosureArrow());
         rightArrow.addStyleName(RES.styles().rightArrow());
         panel.addEast(rightArrow, 28);
         
         if (page.getImage() == null)
         {
            panel.addWest(new Label(""), 28);
         }
         else
         {
            Image icon = new Image(page.getImage());
            icon.addStyleName(RES.styles().leftIcon());
            panel.addWest(icon, 28);
         }
         
         Label mainLabel = new Label(page.getTitle());
         mainLabel.addStyleName(RES.styles().label());
         mainLabel.getElement().setAttribute("title", page.getSubTitle());
         panel.add(mainLabel);
         
         panel.addDomHandler(handler, ClickEvent.getType());
         
         initWidget(panel);
      }
   }
   
   public interface Styles extends CssResource
   {
      String leftIcon();
      String rightArrow();
      String label();
   }

   public interface Resources extends ClientBundle
   {
      @Source("NewDirectoryNavigationPage.css")
      Styles styles();
   }

   private static Resources RES = GWT.create(Resources.class);
   static { RES.styles().ensureInjected(); }
}
