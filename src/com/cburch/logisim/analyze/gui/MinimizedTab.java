/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.analyze.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.cburch.logisim.analyze.model.AnalyzerModel;
import com.cburch.logisim.analyze.model.OutputExpressions;
import com.cburch.logisim.analyze.model.OutputExpressionsEvent;
import com.cburch.logisim.analyze.model.OutputExpressionsListener;

class MinimizedTab extends AnalyzerTab {
    private class MyListener
            implements OutputExpressionsListener, ActionListener, ItemListener {
        public void expressionChanged(OutputExpressionsEvent event) {
            String output = getCurrentVariable();
            if (event.getType() == OutputExpressionsEvent.OUTPUT_MINIMAL
                    && event.getVariable().equals(output)) {
                minimizedExpr.setExpression(outputExprs.getMinimalExpression(output));
                MinimizedTab.this.validate();
            }
            setAsExpr.setEnabled(output != null && !outputExprs.isExpressionMinimal(output));
        }
        
        public void actionPerformed(ActionEvent event) {
            String output = getCurrentVariable();
            outputExprs.setExpression(output, outputExprs.getMinimalExpression(output));
        }

        public void itemStateChanged(ItemEvent event) {
            updateTab();
        }
    }
    
    private OutputSelector selector;
    private KarnaughMapPanel karnaughMap;
    private ExpressionView minimizedExpr = new ExpressionView();
    private JButton setAsExpr = new JButton();

    private MyListener myListener = new MyListener();
    private OutputExpressions outputExprs;
    
    public MinimizedTab(AnalyzerModel model) {
        this.outputExprs = model.getOutputExpressions();
        outputExprs.addOutputExpressionsListener(myListener);
        
        selector = new OutputSelector(model);
        selector.addItemListener(myListener);
        karnaughMap = new KarnaughMapPanel(model);
        karnaughMap.addMouseListener(new TruthTableMouseListener());
        setAsExpr.addActionListener(myListener);
        
        JPanel buttons = new JPanel(new GridLayout(1, 1));
        buttons.add(setAsExpr);
        
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        setLayout(gb);
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.fill = GridBagConstraints.BOTH;
        
        gb.setConstraints(selector, gc); add(selector);
        gb.setConstraints(karnaughMap, gc); add(karnaughMap);
          Insets oldInsets = gc.insets;
          gc.insets = new Insets(20, 0, 0, 0);
        gb.setConstraints(minimizedExpr, gc); add(minimizedExpr);
          gc.insets = oldInsets;
          gc.fill = GridBagConstraints.NONE;
        gb.setConstraints(buttons, gc); add(buttons);

        String selected = selector.getSelectedOutput();
        setAsExpr.setEnabled(selected != null
                && !outputExprs.isExpressionMinimal(selected));
    }
    
    @Override
    void localeChanged() {
        selector.localeChanged();
        karnaughMap.localeChanged();
        minimizedExpr.localeChanged();
        setAsExpr.setText(Strings.get("minimizedSetButton"));
    }

    @Override
    void updateTab() {
        String output = getCurrentVariable();
        karnaughMap.setOutput(output);
        minimizedExpr.setExpression(outputExprs.getMinimalExpression(output));
        setAsExpr.setEnabled(output != null && !outputExprs.isExpressionMinimal(output));
    }
    
    private String getCurrentVariable() {
        return selector.getSelectedOutput();
    }
}
