/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.framework.component.needproducer.impl;

import com.hp.hpl.jena.rdf.model.Model;
import won.bot.framework.component.needproducer.NeedProducer;

import java.util.HashSet;
import java.util.Set;

/**
 * NeedProducer that needFactories to a list of need factories. Order of delegate request is not guaranteed.
 * Not thread safe.
 */
public abstract class AbstractCompositeNeedProducer implements NeedProducer
{
  private Set<NeedProducer> needFactories = new HashSet<NeedProducer>();

  @Override
  public Model create()
  {
    NeedProducer delegate = selectNonExhaustedNeedFactory();
    if (delegate == null) return null; //we're exhausted
    return delegate.create();
  }
  @Override
  public Model create(Class clazz)
  {
      NeedProducer delegate = selectActiveNeedFactoryOfType(clazz);
      if (delegate == null) return null;
      return delegate.create();
  }

  @Override
  public boolean isExhausted()
  {
    return selectNonExhaustedNeedFactory() == null;
  }

  private NeedProducer selectNonExhaustedNeedFactory(){
    NeedProducer delegate = null;
    //keep fetching delegates, and remove them from the list if they are exhausted
    while ((delegate = selectActiveNeedFactory()) != null && delegate.isExhausted()){
      //here we have a non-null delegate that is exhausted. Remove it
      this.needFactories.remove(delegate);
      delegate = null;
    }
    //here, a non-null delegate will not be exhausted. If it is null, we're completely exhausted
    return delegate;
  }

  /**
   * Returns one of the NeedProducer objects found in the needFactories set, or null if no
   * factories are elegible (in which case the factory is exhausted).
   * @return
   */
  protected abstract NeedProducer selectActiveNeedFactory();

  protected abstract NeedProducer selectActiveNeedFactoryOfType(Class clazz);
  
  protected Set<NeedProducer> getNeedFactories(){
    return needFactories;
  }

  public void setNeedFactories(final Set<NeedProducer> delegates)
  {

    this.needFactories = delegates;
  }

  public void addNeedFactory(NeedProducer needProducer){
    this.needFactories.add(needProducer);
  }

}
